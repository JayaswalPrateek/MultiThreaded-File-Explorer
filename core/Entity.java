package core;

interface Entity {

    ErrorCode create();

    ErrorCode copy();

    ErrorCode delete();

    ErrorCode move();

    ErrorCode rename();

}

interface File extends Entity {
    ErrorCode open();

    ErrorCode properties();
}

interface Folder extends Entity {
    ErrorCode list();

    ErrorCode stepIn();

    ErrorCode stepOut();
}