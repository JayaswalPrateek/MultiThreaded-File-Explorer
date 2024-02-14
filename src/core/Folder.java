package core;

import java.util.concurrent.CopyOnWriteArrayList;

interface Folder extends Entity {
    CopyOnWriteArrayList<String> listFiles();

    CopyOnWriteArrayList<String> listFolders();

    CopyOnWriteArrayList<String> regexFilter(final String patternString);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();
}