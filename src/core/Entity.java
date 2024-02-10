package core;

interface Entity extends Runnable {

    ErrorCode create(final String... names);

    ErrorCode create(final String destination, final String... names);

    ErrorCode copy(final String destination);

    ErrorCode copy(final String newName, final String destination);

    ErrorCode delete(final String... names);

    ErrorCode delete(final String destination, final String... names);

    ErrorCode move(final String destination);

    ErrorCode move(final String destination, final String... newName);

    ErrorCode rename(final String newName);

}

interface iFile extends Entity {
    ErrorCode open();

    ErrorCode properties();
}

interface iFolder extends Entity {
    ErrorCode list();

    ErrorCode regexFilter(final String pattern);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();
}