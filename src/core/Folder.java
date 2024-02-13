package core;

import java.util.concurrent.CopyOnWriteArrayList;

interface Folder extends Entity {
    CopyOnWriteArrayList<String> listFiles();

    CopyOnWriteArrayList<String> listFolders();

    ErrorCode regexFilter(final String pattern);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();
}