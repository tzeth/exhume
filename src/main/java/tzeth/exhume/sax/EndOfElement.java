package tzeth.exhume.sax;

import static com.google.common.base.Preconditions.checkNotNull;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.annotation.Nullable;

import tzeth.exhume.ExhumeException;

public final class EndOfElement {
    private final String uri;
    private final String localName;
    private final String qName;
    private final String value;

    public EndOfElement(@Nullable String uri, @Nullable String localName, String qName,
            @Nullable String value) {
        this.uri = uri;
        this.localName = localName;
        this.qName = checkNotEmpty(qName);
        this.value = checkNotNull(value);
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

    @Nullable
    public String value() {
        return value;
    }

    @Nullable
    public Integer valueAsInteger() {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ExhumeException(e);
        }
    }

    @Nullable
    public Double valueAsDouble() {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ExhumeException(e);
        }
    }

    @Nullable
    public BigDecimal valueAsBigDecimal() {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            throw new ExhumeException(e);
        }
    }

    @Nullable
    public LocalDate valueAsLocalDate() {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ExhumeException(e);
        }
    }

    @Nullable
    public Boolean valueAsBoolean() {
        if (value == null) {
            return null;
        }
        if (value.equals("1") || value.equals("true")) {
            return Boolean.TRUE;
        } else if (value.equals("0") || value.equals("false")) {
            return Boolean.FALSE;
        }
        throw new ExhumeException("Not a valid boolean: " + value);
    }

}
