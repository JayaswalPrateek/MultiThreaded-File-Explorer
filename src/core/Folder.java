package core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

interface Folder extends Entity {
    ErrorCode createNewFile(final String destination, final String... newFileName);

    public ErrorCode createNewFile(final String... newFileNames);

    Future<ErrorCode> copy(final String srcPath, final String srcName, final String destPath, final String destName);

    Future<ErrorCode> copy(final String destination, final String... names);

    Future<ErrorCode> move(final String srcPath, final String srcName, final String destPath, final String destName);

    Future<ErrorCode> move(final String destination, final String... names);

    Future<ErrorCode> rename(final String oldName, final String newName);

    CopyOnWriteArrayList<String> listFiles(final ListOption opt);

    CopyOnWriteArrayList<String> listFiles();

    CopyOnWriteArrayList<String> listFolders(final ListOption opt);

    CopyOnWriteArrayList<String> listFolders();

    CopyOnWriteArrayList<String> regexFilter(final String patternString, final ListOption opt);

    CopyOnWriteArrayList<String> regexFilter(final String patternString);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();

    ErrorCode cd(final String destination);

    ErrorCode cd();

}