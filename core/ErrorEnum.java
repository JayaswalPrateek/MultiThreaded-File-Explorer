package core;

enum ErrorCode {
    SUCCESS(0, "Success"),
    GENERAL_ERROR(1, "General Error"),
    FILE_NOT_FOUND(2, "File Not Found"),
    ACCESS_DENIED(3, "Access Denied"),
    INVALID_OPERATION(4, "Invalid Operation");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return GENERAL_ERROR;
    }
}