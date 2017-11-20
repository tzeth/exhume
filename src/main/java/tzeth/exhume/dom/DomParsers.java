package tzeth.exhume.dom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DomParsers {
	public static Document parseFile(File file) throws SAXException, IOException {
		DocumentBuilder builder = createBuilder();
		return builder.parse(file);
	}

	public static Document parseStream(InputStream inputStream) throws SAXException, IOException {
		DocumentBuilder builder = createBuilder();
		return builder.parse(inputStream);
	}

	public static Document parseSource(InputSource source) throws SAXException, IOException {
		DocumentBuilder builder = createBuilder();
		return builder.parse(source);
	}
	
	public static Document parseXml(String xml) throws SAXException {
		try {
			InputSource source = new InputSource(new StringReader(xml));
			return parseSource(source);
		} catch (IOException e) {
			// No IO is being done
			throw new RuntimeException(e);
		}
	}
	
	private static DocumentBuilder createBuilder() {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			return builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private DomParsers() {/**/}

}
