package core;

interface File extends Entity {
    ErrorCode open();

    ErrorCode properties();
}
