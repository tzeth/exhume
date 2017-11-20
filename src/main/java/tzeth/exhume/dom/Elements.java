package tzeth.exhume.dom;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;

import tzeth.exhume.ExhumeException;

public final class Elements implements Iterable<Element> {
    private final NodeList nodeList;

    public Elements(NodeList nodeList) {
        this.nodeList = checkNotNull(nodeList);
    }

    /**
     * Returns all elements that are the children of {@code node}.
     */
    public static Elements under(Node node) {
        return new Elements(node.getChildNodes());
    }

    public static Elements matching(Node node, String expression) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(node,
                    XPathConstants.NODESET);
            return new Elements(nodes);
        } catch (XPathExpressionException e) {
            throw new ExhumeException(e);
        }
    }

    @Override
    public Iterator<Element> iterator() {
        return new IteratorImpl();
    }

    public ImmutableList<Element> asList() {
        ImmutableList.Builder<Element> builder = ImmutableList.builder();
        for (int n = 0; n < nodeList.getLength(); ++n) {
            Node node = nodeList.item(n);
            if (node instanceof Element) {
                builder.add((Element) node);
            }
        }
        return builder.build();
    }

    private class IteratorImpl implements Iterator<Element> {
        private int index;
        @Nullable
        private Element next;

        public IteratorImpl() {
            this.next = getNext();
        }

        private Element getNext() {
            for (; index < nodeList.getLength(); ++index) {
                Node node = nodeList.item(index);
                if (node instanceof Element) {
                    ++index;
                    return (Element) node;
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Element next() {
            checkState(hasNext());
            Element ret = this.next;
            this.next = getNext();
            return ret;
        }
    }

}
