package github.com.mgrzeszczak.reactor.core;

public final class ReactorException extends RuntimeException {

    ReactorException() {
    }

    ReactorException(String message) {
        super(message);
    }

    ReactorException(String message, Throwable cause) {
        super(message, cause);
    }

    ReactorException(Throwable cause) {
        super(cause);
    }

    ReactorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
