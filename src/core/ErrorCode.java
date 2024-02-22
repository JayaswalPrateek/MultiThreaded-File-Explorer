package core;

public enum ErrorCode {
    SUCCESS(0, "Success"),
    DIR_ALREADY_EXISTS(1, "Directory already exists"),
    DIR_NOT_EMPTY(2, "Directory is not empty"),
    DIR_NOT_FOUND(3, "Folder Not Found"),
    ENTITY_IS_LOCKED(4, "Cannot work with locked entities"),
    ENTITY_NOT_FOUND(5, "No such file/folder found"),
    FILE_ALREADY_EXISTS(6, "File already exists"),
    FILE_NOT_FOUND(7, "File Not Found"),
    ILLEGAL_NAME(8, "Name contains Illegal Character"),
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