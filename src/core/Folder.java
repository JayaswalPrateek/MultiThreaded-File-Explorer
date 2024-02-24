package core;

import java.util.concurrent.CopyOnWriteArrayList;

interface Folder extends Entity {

    CopyOnWriteArrayList<String> listFiles(final ListOption opt);

    CopyOnWriteArrayList<String> listFiles();

    CopyOnWriteArrayList<String> listFolders(final ListOption opt);

    CopyOnWriteArrayList<String> listFolders();

    CopyOnWriteArrayList<String> regexFilter(final String patternString, final ListOption opt);

    CopyOnWriteArrayList<String> regexFilter(final String patternString);

    ErrorCode createNewFile(final String destination, final String... newFileName);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();

    ErrorCode cd(final String destination);
}