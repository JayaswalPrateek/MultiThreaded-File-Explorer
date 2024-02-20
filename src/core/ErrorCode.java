package core;

public enum ErrorCode {

    SUCCESS(0, "Success"),
    DIR_ALREADY_EXISTS(1, "Directory already exists"),
    DIR_IS_LOCKED(2, "Cannot work with locked Directories"),
    DIR_NOT_EMPTY(3, "Directory is not empty"),
    DIR_NOT_FOUND(4, "Folder Not Found"),
    ENTITY_IS_LOCKED(5, "Cannot work with locked entities"),
    FILE_ALREADY_EXISTS(6, "File already exists"),
    FILE_IS_LOCKED(7, "Cannot work with locked files"),
    FILE_NOT_FOUND(8, "File Not Found"),
    ILLEGAL_NAME(9, "Name contains Illegal Character"),
    IO_ERROR(10, "IO Exception"),
    OPERATION_NOT_SUPPORTED(11, "Operation Not Supported"),
    UNKOWN_ERROR(12, "Unkown Error");

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