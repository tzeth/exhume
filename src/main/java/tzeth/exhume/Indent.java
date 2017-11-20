package tzeth.exhume;

import static com.google.common.base.Preconditions.checkState;
import static tzeth.preconds.MorePreconditions.checkNotEmpty;
import static tzeth.preconds.MorePreconditions.checkNotNegative;
import static tzeth.preconds.MorePreconditions.checkPositive;

import java.io.IOException;

import com.google.common.base.Strings;

public final class Indent {
    public static Indent tab() {
        return new Indent("\t", 0);
    }

    public static Indent spaces(int count) {
        checkPositive(count);
        return new Indent(Strings.repeat(" ", count), 0);
    }

    private final String unit;
    private final int level;

    public Indent(String unit, int level) {
        this.unit = checkNotEmpty(unit);
        this.level = checkNotNegative(level);
    }

    public Indent increase() {
        return new Indent(this.unit, this.level + 1);
    }

    public Indent decrease() {
        checkState(this.level > 0);
        return new Indent(this.unit, this.level - 1);
    }

    public void appendTo(StringBuilder sb) {
        if (this.level > 0) {
            sb.append(toString());
        }
    }

    public void appendTo(Appendable target) throws IOException {
        if (this.level > 0) {
            target.append(toString());
        }
    }

    @Override
    public String toString() {
        return Strings.repeat(this.unit, this.level);
    }

}
