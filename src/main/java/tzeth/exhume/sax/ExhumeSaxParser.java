package tzeth.exhume.sax;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import tzeth.exhume.ExhumeException;

public final class ExhumeSaxParser {
    private final RegisteredHandlers<StartElementHandler> startElementHandlers = new RegisteredHandlers<>();
    private final RegisteredHandlers<EndElementHandler> endElementHandlers = new RegisteredHandlers<>();

    public ExhumeSaxParser(Object... handlers) {
        for (Object o : handlers) {
            registerHandler(o);
        }
    }

    public void registerHandler(Object handler) {
        String rootPath = "";
        if (handler.getClass().isAnnotationPresent(RootPath.class)) {
            rootPath = handler.getClass().getAnnotation(RootPath.class).value();
        }
        for (Method m : handler.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(ElementStart.class)) {
                String leafPath = m.getAnnotation(ElementStart.class).value();
                PathExpression pathExpression = PathExpression.of(rootPath, leafPath);
                StartElementHandler seh = new StartElementHandler(handler, m);
                startElementHandlers.add(pathExpression, seh);
            } else if (m.isAnnotationPresent(ElementEnd.class)) {
                String leafPath = m.getAnnotation(ElementEnd.class).value();
                PathExpression pathExpression = PathExpression.of(rootPath, leafPath);
                EndElementHandler eeh = new EndElementHandler(handler, m);
                endElementHandlers.add(pathExpression, eeh);
            }
        }
    }

    public void parseXml(String xml) throws SAXException {
        SaxParsers.parseXml(xml, new HandlerImpl());
    }

    public void parseFile(File file) throws SAXException, IOException {
        SaxParsers.parseFile(file, new HandlerImpl());
    }

    public void parseStream(InputStream stream) throws SAXException, IOException {
        SaxParsers.parseStream(stream, new HandlerImpl());
    }

    private class HandlerImpl extends DefaultHandler {
        private final Stack<String> pathStack = new Stack<>();
        private final Stack<StringBuilder> values = new Stack<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            pathStack.push(qName);
            values.push(new StringBuilder());
            invokeStartElementHandlers(uri, localName, qName, attributes);
        }

        private void invokeStartElementHandlers(String uri, String localName, String qName,
                Attributes attributes) {
            if (startElementHandlers.isEmpty()) {
                return;
            }
            Path path = currentPath();
            Collection<StartElementHandler> handlers = startElementHandlers.get(path);
            if (handlers.isEmpty()) {
                return;
            }
            StartOfElement soe = new StartOfElement(uri, localName, qName, attributes);
            handlers.forEach(seh -> seh.invoke(soe));
        }

        private Path currentPath() {
            String s = pathStack.stream()
                    .collect(Collectors.joining(Path.SEPARATOR, Path.SEPARATOR, ""));
            return Path.of(s);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // TODO: Distinguish between null and empty values. Must look at the "xsi:nil"
            // attribute
            // in the start element.
            StringBuilder value = values.pop();
            invokeEndElementHandlers(uri, localName, qName, value.toString());
            pathStack.pop();
        }

        private void invokeEndElementHandlers(String uri, String localName, String qName,
                @Nullable String value) {
            if (endElementHandlers.isEmpty()) {
                return;
            }
            Path path = currentPath();
            Collection<EndElementHandler> handlers = endElementHandlers.get(path);
            if (handlers.isEmpty()) {
                return;
            }
            EndOfElement eoe = new EndOfElement(uri, localName, qName, value.toString());
            handlers.forEach(eeh -> eeh.invoke(eoe));
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (!values.isEmpty()) {
                StringBuilder value = values.peek();
                value.append(ch, start, length);
            }
        }
    }

    private static class StartElementHandler {
        private final Object object;
        private final Method method;

        public StartElementHandler(Object object, Method method) {
            this.object = object;
            this.method = method;
            Class<?>[] parameterTypes = method.getParameterTypes();
            checkArgument(
                    parameterTypes.length == 1 && parameterTypes[0].equals(StartOfElement.class),
                    "An ElementStart handler must take exactly one parameter of type StartOfElement as input");
        }

        public void invoke(StartOfElement soe) {
            try {
                method.setAccessible(true);
                method.invoke(object, soe);
            } catch (IllegalAccessException e) {
                throw new ExhumeException(e);
            } catch (IllegalArgumentException e) {
                throw new ExhumeException(e);
            } catch (InvocationTargetException e) {
                throw new ExhumeException(e.getCause());
            }
        }
    }

    private static class EndElementHandler {
        private final Object object;
        private final Method method;
        private final ValueFactory valueFactory;

        public EndElementHandler(Object object, Method method) {
            this.object = object;
            this.method = method;
            Class<?>[] parameterTypes = method.getParameterTypes();
            checkArgument(parameterTypes.length == 1,
                    "An ElementEnd handler must take exactly one parameter as input");
            this.valueFactory = getValueFactory(parameterTypes[0]);
        }

        private ValueFactory getValueFactory(Class<?> type) {
            if (type.equals(EndOfElement.class)) {
                return eoe -> eoe;
            } else if (type.equals(String.class)) {
                return EndOfElement::value;
            } else if (type.equals(Integer.class)) {
                return EndOfElement::valueAsInteger;
            } else if (type.equals(Double.class)) {
                return EndOfElement::valueAsDouble;
            } else if (type.equals(BigDecimal.class)) {
                return EndOfElement::valueAsBigDecimal;
            } else if (type.equals(Boolean.class)) {
                return EndOfElement::valueAsBoolean;
            } else if (type.equals(LocalDate.class)) {
                return EndOfElement::valueAsLocalDate;
            } else {
                throw new IllegalArgumentException(
                        "An ElementEnd handler must take one of the following types as input: "
                                + "EndOfElement, String, Integer, Double, BigDecimal, Boolean, LocalDate.");
            }
        }

        public void invoke(EndOfElement eoe) {
            try {
                Object value = valueFactory.of(eoe);
                method.setAccessible(true);
                method.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new ExhumeException(e);
            } catch (IllegalArgumentException e) {
                throw new ExhumeException(e);
            } catch (InvocationTargetException e) {
                throw new ExhumeException(e.getCause());
            }
        }

        @FunctionalInterface
        private static interface ValueFactory {
            public Object of(EndOfElement eoe);
        }
    }

    private static class RegisteredHandlers<T> {
        private final Multimap<PathExpression, T> all = HashMultimap.create();
        private final Multimap<Path, T> cachedForPath = HashMultimap.create();

        public void add(PathExpression pathExpression, T handler) {
            all.put(pathExpression, handler);
        }

        public boolean isEmpty() {
            return all.isEmpty();
        }

        public Collection<T> get(Path path) {
            if (cachedForPath.containsKey(path)) {
                return cachedForPath.get(path);
            }
            List<T> handlers = new ArrayList<>();
            for (PathExpression expr : all.keySet()) {
                if (expr.matches(path)) {
                    handlers.addAll(all.get(expr));
                }
            }
            cachedForPath.putAll(path, handlers);
            return handlers;
        }
    }

}
