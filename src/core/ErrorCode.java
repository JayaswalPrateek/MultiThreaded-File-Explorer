package core;

public enum ErrorCode {
    SUCCESS(0, "Success"),
    FILE_NOT_FOUND(1, "File Not Found"),
    FOLDER_NOT_FOUND(2, "Folder Not Found"),
    OPERATION_NOT_SUPPORTED(3, "Operation Not Supported"),
    ILLEGAL_NAME(4, "Name contains Illegal Character");

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