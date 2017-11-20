package tzeth.exhume;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

public final class XmlBuilder {
    // TODO: Escaping

    private final Charset charset;
    private Element root;
    private final Stack<Element> stack = new Stack<>();

    public XmlBuilder() {
        this(StandardCharsets.UTF_8);
    }

    public XmlBuilder(Charset charset) {
        this.charset = checkNotNull(charset);
    }

    public Element root(String name) {
        checkState(this.root == null);
        this.root = new Element(name);
        this.stack.push(this.root);
        return this.root;
    }

    public String toXml() {
        return toXml(Indent.spaces(2));
    }

    public String toXml(Indent indent) {
        checkState(this.root != null);
        StringBuilder xml = new StringBuilder();
        try {
            writeTo(xml, indent);
        } catch (IOException e) {
            // Will not happen since we're appending to a StringBuilder
            throw new RuntimeException(e);
        }
        return xml.toString();
    }

    private void writeTo(Appendable xml, Indent indent) throws IOException {
        xml.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\" ?>")
                .append(System.lineSeparator());
        root.write(xml, indent);
    }

    public void writeToFile(File file) throws IOException {
        writeToFile(file, Indent.spaces(2));
    }

    public void writeToFile(File file, Indent indent) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                charset)) {
            writeTo(writer, indent);
        }
    }

    public final class Element {
        private final String name;
        private final Map<String, String> attributes = new HashMap<>();
        private final List<Element> children = new ArrayList<>();
        @Nullable
        private String value;

        public Element(String name) {
            this.name = checkNotEmpty(name);
        }

        public Element attribute(String name, String value) {
            attributes.put(checkNotEmpty(name), checkNotNull(value));
            return this;
        }

        public Element attribute(String name, Object value) {
            return attribute(name, String.valueOf(checkNotNull(value)));
        }

        public Element withValue(@Nullable String value) {
            this.value = value;
            mixedContentNotSupported();
            return this;
        }

        private void mixedContentNotSupported() {
            boolean mixedContent = !Strings.isNullOrEmpty(this.value) && (this.children.size() > 0);
            checkState(!mixedContent, "Mixed content is not supported");
        }

        public Element child(String name) {
            Element child = new Element(name);
            this.children.add(child);
            mixedContentNotSupported();
            XmlBuilder.this.stack.push(child);
            return child;
        }

        public Element close() {
            // FIXME: Very ugly to return null if the root element is closed.
            XmlBuilder.this.stack.pop();
            return XmlBuilder.this.stack.isEmpty() ? null : XmlBuilder.this.stack.peek();
        }

        private void write(Appendable xml, Indent indent) throws IOException {
            indent.appendTo(xml);
            xml.append("<").append(name);
            for (Map.Entry<String, String> a : this.attributes.entrySet()) {
                xml.append(" ").append(a.getKey()).append("=\"").append(a.getValue()).append("\"");
            }
            if (Strings.isNullOrEmpty(this.value) && this.children.isEmpty()) {
                xml.append(" />");
                return;
            }
            xml.append(">");
            if (Strings.isNullOrEmpty(this.value)) {
                assert !this.children.isEmpty();
                xml.append(System.lineSeparator());
                Indent nextIndent = indent.increase();
                for (Element child : this.children) {
                    child.write(xml, nextIndent);
                    xml.append(System.lineSeparator());
                }
                indent.appendTo(xml);
                closeTag(xml);
            } else {
                assert this.children.isEmpty();
                xml.append(value);
                closeTag(xml);
            }
        }

        public void closeTag(Appendable xml) throws IOException {
            xml.append("</").append(name).append(">");
        }
    }

}
