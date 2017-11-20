package tzeth.exhume.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tzeth.exhume.ExhumeException;

public final class SaxParsers {

    public static void parseFile(File file, DefaultHandler handler)
            throws SAXException, IOException {
        parseFile(file, handler, false);
    }

    public static void parseFile(File file, DefaultHandler handler, boolean namespaceAware)
            throws SAXException, IOException {
        SAXParser p = createParser(namespaceAware);
        p.parse(file, handler);
    }

    public static void parseStream(InputStream is, DefaultHandler handler)
            throws SAXException, IOException {
        parseStream(is, handler, false);
    }

    public static void parseStream(InputStream is, DefaultHandler handler, boolean namespaceAware)
            throws SAXException, IOException {
        SAXParser p = createParser(namespaceAware);
        p.parse(is, handler);
    }

    public static void parseSource(InputSource is, DefaultHandler handler)
            throws SAXException, IOException {
        parseSource(is, handler, false);
    }

    public static void parseSource(InputSource is, DefaultHandler handler, boolean namespaceAware)
            throws SAXException, IOException {
        SAXParser p = createParser(namespaceAware);
        p.parse(is, handler);
    }

    public static void parseXml(String xml, DefaultHandler handler) throws SAXException {
        parseXml(xml, handler, false);
    }

    public static void parseXml(String xml, DefaultHandler handler, boolean namespaceAware)
            throws SAXException {
        try {
            InputSource is = new InputSource(new StringReader(xml));
            parseSource(is, handler, namespaceAware);
        } catch (IOException e) {
            // No IO is done
            throw new ExhumeException(e);
        }
    }

    private static SAXParser createParser(boolean namespaceAware) {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setNamespaceAware(namespaceAware);
            return f.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new ExhumeException(e);
        }
    }

    private SaxParsers() {/**/}

}
