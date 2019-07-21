package io.gridgo.boot.support.exceptions;

public class NoDataSourceException extends RuntimeException {

    private static final long serialVersionUID = 185513533756870011L;

    public NoDataSourceException(String message) {
        super(message);
    }
}
