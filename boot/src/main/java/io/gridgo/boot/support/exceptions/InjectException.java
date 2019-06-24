package io.gridgo.boot.support.exceptions;

public class InjectException extends RuntimeException {

    private static final long serialVersionUID = 185513533756870011L;

    public InjectException(String message) {
        super(message);
    }

    public InjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
