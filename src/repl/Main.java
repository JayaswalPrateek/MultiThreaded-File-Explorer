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
            System.out.println(DEFAULT_PATH);
            System.out.println("name is " + obj.name);
            System.out.println("path is " + obj.path);

            System.out.println("Folders in " + WORKING_DIR);
            for (final String entity : foldersInDir)
                System.out.println(entity);
            System.out.println("Files in " + WORKING_DIR);
            for (final String entity : filesInDir)
                System.out.println(entity);
            for (final String result : workingDirList.regexFilter("bashrc"))
                System.out.println("Found: " + result);

            System.out.println("now in " + workingDirList.getPath() + workingDirList.getName());
            System.out.println("name is " + workingDirList.getName());
            System.out.println("path is " + workingDirList.getPath());
            System.out.println("Stepping out:");
            System.out.println(workingDirList.stepOut());
            System.out.println("now in " + workingDirList.getPath() + workingDirList.getName());
            System.out.println("new name is " + workingDirList.getName());
            System.out.println("new path is " + workingDirList.getPath());

            System.out.println("Stepping into prateek");
            System.out.println(workingDirList.stepIn("prateek"));
            System.out.println("now in " + workingDirList.getPath() + workingDirList.getName());
            System.out.println("new name is " + workingDirList.getName());
            System.out.println("new path is " + workingDirList.getPath());

            System.out.println("cding into downloads");
            System.out.println(workingDirList.cd("./Downloads"));
            System.out.println("now in " + workingDirList.getPath() + workingDirList.getName());
            System.out.println("new name is " + workingDirList.getName());
            System.out.println("new path is " + workingDirList.getPath());

            System.out.print("Enter a file name: ");
            String fname = "foo.txt";
            System.out.println(fname);
            boolean found = false;
            for (final String fnameWithPath : filesInDir)
                if (fnameWithPath.endsWith(fname)) {
                    entityDetails obj2 = new entityDetails(fnameWithPath);
                    FileImpl f = new FileImpl(obj2.name, obj2.path);
                    found = true;
                    f.properties();
                    System.out.println(f.open());
                }
            if (!found)
                System.out.println(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
