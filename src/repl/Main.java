package repl; // Read-Evaluate-Print Loop

import java.util.concurrent.CopyOnWriteArrayList;
import core.*;

final class Main {
    public static void main(final String[] args) {
        final FolderImpl workingDir = FolderImpl.getInstance();

        System.out.println("You are currently in path=" + workingDir.getPath() + " name=" + workingDir.getName());

        // to list all the folders:
        final CopyOnWriteArrayList<String> directoryList = workingDir.listFolders();
        // listFolders(ListOption.SHOW_HIDDEN); to include hidden folders
        System.out.println("Folders in " + System.getProperty("user.home"));
        for (final String entity : directoryList)
            System.out.println(entity);

        // to list all the files:
        final CopyOnWriteArrayList<String> fileList = workingDir.listFiles();
        // listFiles(ListOption.SHOW_HIDDEN); to include hidden files
        System.out.println("Files in " + System.getProperty("user.home"));
        for (final String entity : fileList)
            System.out.println(entity);

        // to search for a file/folder:
        final String regexQry = "bashrc";
        final CopyOnWriteArrayList<String> resultsWithoutHiddenFilter = workingDir.regexFilter(regexQry);
        if (resultsWithoutHiddenFilter.isEmpty())
            System.out.println(regexQry + " Not Found");
        else
            for (final String result : resultsWithoutHiddenFilter)
                System.out.println("Found: " + result);

        // regexFilter(regexQry, ListOption.SHOW_HIDDEN) to search hidden files too
        // final String regexQry = "bashrc";
        final CopyOnWriteArrayList<String> resultsWithHiddenFilter = workingDir.regexFilter(regexQry,
                ListOption.SHOW_HIDDEN);
        if (resultsWithHiddenFilter.isEmpty())
            System.out.println(regexQry + " Not Found");
        else
            for (final String result : resultsWithHiddenFilter)
                System.out.println("Found: " + result);

        System.out.println("now in " + workingDir.getPath() + workingDir.getName());
        System.out.println("name is " + workingDir.getName());
        System.out.println("path is " + workingDir.getPath());
        System.out.println(workingDir.stepOut());
        System.out.println("now in " + workingDir.getPath() + workingDir.getName());
        System.out.println("new name is " + workingDir.getName());
        System.out.println("new path is " + workingDir.getPath());

        System.out.println(workingDir.stepIn("prateek"));
        System.out.println("now in " + workingDir.getPath() + workingDir.getName());
        System.out.println("new name is " + workingDir.getName());
        System.out.println("new path is " + workingDir.getPath());

        System.out.println(workingDir.cd("./Downloads"));
        System.out.println("now in " + workingDir.getPath() + workingDir.getName());
        System.out.println("new name is " + workingDir.getName());
        System.out.println("new path is " + workingDir.getPath());

        System.out.println("Creating a folder called test");
        System.out.println(workingDir.create(new String[] { "test" }));
        System.out.println(workingDir.cd("test"));

        System.out.println("Creating multiple folders called abc, def, ghi, jkl in test");
        System.out.println(workingDir.create(new String[] { "abc", "def", "ghi",
                "jkl" }));

        System.out.println("Creating file foo.txt in test");
        System.out.println(workingDir.createNewFile(new String[] { "foo.txt" }));

        System.out.println("Creating multiple files a.txt, b.txt, c.txt in test");
        System.out.println(workingDir.createNewFile(new String[] { "a.txt", "b.txt",
                "c.txt" }));

        System.out.println(File.open("foo.txt"));
        System.out.println(File.properties("foo.txt"));

        System.out.println(workingDir.delete("abc"));
        System.out.println(workingDir.delete("ghi", "jkl"));

        System.out.println(workingDir.delete("foo.txt"));
        System.out.println(workingDir.delete("a.txt", "b.txt"));

        System.out.println(workingDir.move(".", "a.txt", ".", "haha.txt"));
        System.out.println(workingDir.move(".", "a.txt", "abc/", "haha.txt"));
        System.out.println(workingDir.move(".", "ghi/", "abc/", "ihj"));
        System.out.println(workingDir.move("abc/", new String[] { "a.txt",
                "b.txt", "c.txt" }));

        System.out.println(workingDir.rename("foo.txt", "bar.txt"));

        System.out.println(workingDir.copy(".", "a.txt", ".", "haha.txt"));
        System.out.println(workingDir.copy(".", "a.txt", "abc/", "haha.txt"));
        System.out.println(workingDir.copy("abc/", new String[] { "a.txt", "b.txt",
                "c.txt" }));
        System.out.println(workingDir.copy(".", "abc/", "def/", "newABC/"));
    }
}
