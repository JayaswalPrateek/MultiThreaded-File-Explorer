package core;

public enum ErrorCode {

    SUCCESS(0, "Success"),
    DIR_ALREADY_EXISTS(1, "Directory already exists"),
    DIR_NOT_EMPTY(2, "Directory is not empty"),
    DIR_NOT_FOUND(3, "Folder Not Found"),
    FILE_ALREADY_EXISTS(4, "File already exists"),
    FILE_NOT_FOUND(5, "File Not Found"),
    ILLEGAL_NAME(6, "Name contains Illegal Character"),
    IO_ERROR(7, "IO Exception"),
    OPERATION_NOT_SUPPORTED(8, "Operation Not Supported"),
    UNKOWN_ERROR(9, "Unkown Error");

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