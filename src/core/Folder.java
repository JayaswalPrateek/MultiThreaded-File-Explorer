package core;

import java.util.concurrent.CopyOnWriteArrayList;

interface Folder extends Entity {
    public enum ListOption {
        SHOW_HIDDEN,
        NONE
    }

    CopyOnWriteArrayList<String> listFiles(final ListOption opt);

    CopyOnWriteArrayList<String> listFolders(final ListOption opt);

    CopyOnWriteArrayList<String> regexFilter(final String patternString);

    ErrorCode createNewFile(final String newFileName);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();

    ErrorCode cd(final String destination);
}