package tzeth.exhume.dom;

import static java.util.Objects.requireNonNull;
import static tzeth.preconds.MorePreconditions.checkNotNegative;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Pretty prints DOM documents.
 */
public final class XmlPrettyPrint {
    private final int indent;

    public XmlPrettyPrint(int indent) {
        this.indent = checkNotNegative(indent);
    }

    public static XmlPrettyPrint withIndent(int indent) {
        return new XmlPrettyPrint(indent);
    }
    
    public void write(Document doc, Writer writer) {
        requireNonNull(doc);
        requireNonNull(writer);
        try {
            DOMSource ds = new DOMSource(doc);
            StreamResult sr = new StreamResult(writer);
            TransformerFactory tf = createTransformerFactory();
            Transformer trans = createTransform(tf);
            trans.transform(ds, sr);
        } catch (TransformerException x) {
            throw new RuntimeException(x);
        }
    }
    
    public String toString(Document doc) {
        StringWriter writer = new StringWriter();
        write(doc, writer);
        return writer.toString();
    }
    
    public void write(Document doc, File file) throws IOException {
        requireNonNull(doc);
        requireNonNull(file);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            write(doc, writer);
        }
    }
    
    private TransformerFactory createTransformerFactory() {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute("indent-number", indent);
        return tf;
    }

    private static Transformer createTransform(TransformerFactory tf) {
        try {
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            return trans;
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }
}
