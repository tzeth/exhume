package tzeth.exhume.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

import tzeth.exhume.XmlBuilder;
import tzeth.exhume.dom.DomParsers;
import tzeth.exhume.dom.Elements;

public final class ElementsTest {

    @Test
    public void testUnder() {
        try {
            XmlBuilder xmlBuilder = new XmlBuilder();
            xmlBuilder.root("root")
                .child("x").withValue("A").close()
                .child("x").withValue("B").close()
                .child("x").withValue("C").close()
                .child("y").withValue("Z").close();
            String xml = xmlBuilder.toXml();
            Document dom = DomParsers.parseXml(xml);
            ImmutableList<Element> xs = Elements.matching(dom, "root/x").asList();
            assertEquals("Wrong number of elements", 3, xs.size());
            List<String> values = xs.stream()
                    .map(Element::getTextContent)
                    .collect(Collectors.toList());
            assertEquals("Unexpected values", Arrays.asList("A", "B", "C"), values);
        } catch (SAXException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIterator() {
        try {
            XmlBuilder xmlBuilder = new XmlBuilder();
            xmlBuilder.root("root")
                .child("x").withValue("A").close()
                .child("x").withValue("B").close()
                .child("x").withValue("C").close()
                .child("y").withValue("Z").close();
            String xml = xmlBuilder.toXml();
            Elements children = Elements.under(DomParsers.parseXml(xml).getDocumentElement());
            List<String> expected = Arrays.asList("A", "B", "C", "Z");
            List<String> actual = new ArrayList<>();
            children.forEach(e -> actual.add(e.getTextContent()));
            assertEquals("Unexpected values", expected, actual);
        } catch (SAXException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEmptyIterator() {
        try {
            Elements children = Elements.under(DomParsers.parseXml("<root/>").getDocumentElement());
            List<String> actual = new ArrayList<>();
            children.forEach(e -> actual.add(e.getTextContent()));
            assertTrue(actual.isEmpty());
        } catch (SAXException e) {
            fail(e.getMessage());
        }
    }

}
