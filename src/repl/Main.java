package repl; // Read-Evaluate-Print Loop

import java.util.concurrent.CopyOnWriteArrayList;
import core.*;

final class Main {
    private static final boolean DEBUG = true;

    private static final class Splitter {
        private final String path, name;

        Splitter(final String pathWithName) {
            path = pathWithName.substring(0, 1 + pathWithName.lastIndexOf('/'));
            name = pathWithName.substring(1 + pathWithName.lastIndexOf('/'));
            if (DEBUG)
                System.out.println("Splitting " + pathWithName + " into " + path + " and " + name);
        }
    }

    public static void main(final String[] args) {
        Splitter s = new Splitter(System.getProperty("user.home"));
        FolderImpl workingDir = new FolderImpl(s.path, s.name);
        CopyOnWriteArrayList<String> directoryList = workingDir.listFolders();
        CopyOnWriteArrayList<String> fileList = workingDir.listFiles();
        if (DEBUG) {
            System.out.println(System.getProperty("user.home"));
            System.out.println("name is " + s.name);
            System.out.println("path is " + s.path);

            System.out.println("Folders in " + System.getProperty("user.home"));
            for (final String entity : directoryList)
                System.out.println(entity);
            System.out.println("Files in " + System.getProperty("user.home"));
            for (final String entity : fileList)
                System.out.println(entity);
            for (final String result : workingDir.regexFilter("bashrc"))
                System.out.println("Found: " + result);

            System.out.println("now in " + workingDir.getPath() + workingDir.getName());
            System.out.println("name is " + workingDir.getName());
            System.out.println("path is " + workingDir.getPath());
            System.out.println("Stepping out:");
            System.out.println(workingDir.stepOut());
            System.out.println("now in " + workingDir.getPath() + workingDir.getName());
            System.out.println("new name is " + workingDir.getName());
            System.out.println("new path is " + workingDir.getPath());

            System.out.println("Stepping into prateek");
            System.out.println(workingDir.stepIn("prateek"));
            System.out.println("now in " + workingDir.getPath() + workingDir.getName());
            System.out.println("new name is " + workingDir.getName());
            System.out.println("new path is " + workingDir.getPath());

            System.out.println("cding into downloads");
            System.out.println(workingDir.cd("./Downloads"));
            System.out.println("now in " + workingDir.getPath() + workingDir.getName());
            System.out.println("new name is " + workingDir.getName());
            System.out.println("new path is " + workingDir.getPath());

            System.out.print("Enter a file name: ");
            String fname = "foo.txt";
            System.out.println(fname);

            boolean found = false;
            for (final String fnameWithPath : fileList)
                if (fnameWithPath.endsWith('/' + fname)) {
                    s = new Splitter(fnameWithPath);
                    FileImpl f = new FileImpl(s.path, s.name);
                    found = true;
                    f.properties();
                    System.out.println(f.open());
                }
            if (!found)
                System.out.println(ErrorCode.FILE_NOT_FOUND);

            // 2 ways to create a dir:
            // 1:
            String starray1[] = { "DIR_A" };
            workingDir.create(".", starray1);
            String starray2[] = { "DIR_B", "DIR_C" };
            workingDir.create(".", starray2);
            String starray3[] = { "DIR_D" };
            workingDir.create(starray3);
            String starray4[] = { "DIR_E", "DIR_F" };
            workingDir.create(starray4);
            // 2:
            FolderImpl newFolder = new FolderImpl("newFolder", workingDir);

            // 2 ways of creating a file:
            // 1:
            FileImpl newFile = new FileImpl("test.txt", workingDir);
            // 2:
            newFile.create(new String[] { "a.txt", "b.txt" });
        }
    }
}
