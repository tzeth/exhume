package tzeth.exhume.dom;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class Attributes implements Iterable<Attr> {
    private final NamedNodeMap map;
    
    private Attributes(NamedNodeMap map) {
        this.map = requireNonNull(map);
    }

    public static Attributes in(Element e) {
        return new Attributes(e.getAttributes());
    }
    
    @Override
    public Iterator<Attr> iterator() {
        return new IteratorImpl();
    }
    
    private class IteratorImpl implements Iterator<Attr> {
        private int index;
        @Nullable
        private Attr next;
        
        public IteratorImpl() {
            this.next = getNext();
        }

        private Attr getNext() {
            for (; index < map.getLength(); ++index) {
                Node node = map.item(index);
                if (node instanceof Attr) {
                    ++index;
                    return (Attr) node;
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Attr next() {
            checkState(hasNext());
            Attr ret = this.next;
            this.next = getNext();
            return ret;
        }
    }
}
