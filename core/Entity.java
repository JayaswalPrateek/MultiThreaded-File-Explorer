package core;

interface Entity {

    ErrorCode create(final String name); // defaults to current path

    ErrorCode create(final String name, final String destination);

    ErrorCode copy(final String destination); // defaults to current name

    ErrorCode copy(final String newName, final String destination);

    ErrorCode delete();

    ErrorCode move(final String destination); // defaults to current name

    ErrorCode move(final String newName, final String destination);

    ErrorCode rename(final String newName);

}

interface File extends Entity {
    ErrorCode open();

    ErrorCode properties();
}

interface Folder extends Entity {
    ErrorCode list();

    ErrorCode regexFilter(final String pattern);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();
}