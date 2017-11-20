package tzeth.exhume.sax;

import static com.google.common.base.Preconditions.checkNotNull;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;

import javax.annotation.Nullable;

import org.xml.sax.Attributes;

public final class StartOfElement {
    private final String uri;
    private final String localName;
    private final String qName;
    private final Attributes attributes;

    public StartOfElement(@Nullable String uri, @Nullable String localName, String qName,
            Attributes attributes) {
        this.uri = uri;
        this.localName = localName;
        this.qName = checkNotEmpty(qName);
        this.attributes = checkNotNull(attributes);
    }

    @Nullable
    public String uri() {
        return uri;
    }

    @Nullable
    public String localName() {
        return localName;
    }

    public String qName() {
        return qName;
    }

    public Attributes attributes() {
        return attributes;
    }

    @Nullable
    public String attributeValue(String qName) {
        return attributes.getValue(qName);
    }

    @Nullable
    public String attributeValue(String uri, String localName) {
        return attributes.getValue(uri, localName);
    }

}
