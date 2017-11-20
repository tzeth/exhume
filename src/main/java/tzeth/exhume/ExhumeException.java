package tzeth.exhume;

public final class ExhumeException extends RuntimeException {
    public ExhumeException() {
        super();
    }

    public ExhumeException(String message) {
        super(message);
    }

    public ExhumeException(Throwable cause) {
        super(cause);
    }

    public ExhumeException(String message, Throwable cause) {
        super(message, cause);
    }

}
