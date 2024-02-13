package repl; // Read-Evaluate-Print Loop

import java.util.concurrent.CopyOnWriteArrayList;
import core.*;

public class Main {
    private static boolean DEBUG = true;

    static class entityDetails {
        String name, path;

        entityDetails(final String pathWithName) {
            name = pathWithName.substring(1 + pathWithName.lastIndexOf('/'));
            path = pathWithName.substring(0, pathWithName.lastIndexOf('/'));
            if (DEBUG) {
                System.out.println(DEFAULT_PATH);
                System.out.println("name is " + name);
                System.out.println("path is " + path);
            }
        }
    }

    static final String DEFAULT_PATH = System.getProperty("user.home");
    static String WORKING_DIR = DEFAULT_PATH;

    public static void main(final String[] args) {
        entityDetails obj = new entityDetails(WORKING_DIR);
        FolderImpl workingDirList = new FolderImpl(obj.name, obj.path);
        CopyOnWriteArrayList<String> foldersInDir = workingDirList.listFolders();
        CopyOnWriteArrayList<String> filesInDir = workingDirList.listFiles();
        if (DEBUG) {
            System.out.println("Folders in " + WORKING_DIR);
            for (final String entity : foldersInDir)
                System.out.println(entity);
            System.out.println("Files in " + WORKING_DIR);
            for (final String entity : filesInDir)
                System.out.println(entity);
        }
    }
}
