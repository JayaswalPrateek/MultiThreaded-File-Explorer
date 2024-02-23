package core;

public enum ErrorCode {
    SUCCESS(0, "Success"),
    DIR_ALREADY_EXISTS(1, "Directory Already Exists"),
    DIR_NOT_EMPTY(2, "Directory Not Empty"),
    DIR_NOT_FOUND(3, "Directory Not Found"),
    ENTITY_IS_LOCKED(4, "Cannot Use Busy Entity"),
    ENTITY_NOT_FOUND(5, "No Such File/Directory Found"),
    FILE_ALREADY_EXISTS(6, "File Already Exists"),
    FILE_NOT_FOUND(7, "File Not Found"),
    ILLEGAL_NAME(8, "Name Contains Illegal Character"),
    IO_ERROR(9, "IO Exception"),
    OPERATION_NOT_SUPPORTED(10, "Operation Not Supported"),
    UNKOWN_ERROR(11, "Unkown Error");

    private final int code;
    private final String message;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    final public int getCode() {
        return code;
    }

    final public String getMessage() {
        return message;
    }

    final public static ErrorCode getByCode(final int code) throws IllegalArgumentException {
        for (final ErrorCode errorCode : ErrorCode.values())
            if (errorCode.code == code)
                return errorCode;
        throw new IllegalArgumentException("Bad Code: " + code);
    }
}