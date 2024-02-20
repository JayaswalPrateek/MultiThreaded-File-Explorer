package core;

public enum ErrorCode {

    SUCCESS(0, "Success"),
    DIR_NOT_EMPTY(1, "Directory is not empty"),
    DIR_NOT_FOUND(2, "Folder Not Found"),
    FILE_NOT_FOUND(3, "File Not Found"),
    ILLEGAL_NAME(4, "Name contains Illegal Character"),
    IO_ERROR(5, "IO Exception"),
    OPERATION_NOT_SUPPORTED(6, "Operation Not Supported"),
    UNKOWN_ERROR(7, "Unkown Error");

    private final int code;
    private final String message;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
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