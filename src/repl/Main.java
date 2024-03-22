package repl; // Read-Evaluate-Print Loop

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import core.*;

final class Main {
    private void coreAPIdemo() {
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

        ErrorCode result = ErrorCode.UNKOWN_ERROR;
        try {
            result = workingDir.move(".", "a.txt", ".", "haha.txt").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(result);

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

    private static void showHelp() {
        System.out.println("Options:");
        System.out.println("[pwd] Print Working Directory");
        System.out.println("[cd] Change Directory");
        System.out.println("[find] Search Directory (Add -h to search among hidden files too)");
        System.out.println("[prop] File Properties");
        System.out.println("[open] Open File");
        System.out.println("[ls] List (Add -h to list hidden files too)");
        System.out.println("[touch] Create File");
        System.out.println("[mkdir] Create Folder");
        System.out.println("[cp] copy File/Folder (Destination, names)");
        System.out.println("[slowcp] non multithreaded copy(for demo purposes)");
        System.out.println("[mv] move File/Folder");
        System.out.println("[rename] Rename File/Folder");
        System.out.println("[rm] Delete File/Folder");
        System.out.println("[clear] Clears screen");
        System.out.println("[exit] Exit");
        System.out.println("[help] Shows this message");
        System.out.println("");
    }

    public static void main(final String[] args) {
        final FolderImpl workingDir = FolderImpl.getInstance();
        final Scanner sc = new Scanner(System.in);
        showHelp();
        outer: while (true) {
            System.out.print("Prompt: ");
            final String prompt = sc.nextLine().trim();
            String[] argumentArr = prompt.trim().split("\\s+");
            final String cmd = argumentArr[0].toLowerCase();
            if (argumentArr.length > 0) {
                String[] updatedArr = new String[argumentArr.length - 1];
                System.arraycopy(argumentArr, 1, updatedArr, 0, updatedArr.length);
                argumentArr = updatedArr;
            }

            switch (cmd) {
                case "pwd" -> System.out.println(workingDir.getPath() + workingDir.getName());

                case "cd" -> {
                    if (argumentArr.length == 0)
                        System.out.println(workingDir.cd());
                    else if (argumentArr.length == 1)
                        System.out.println(workingDir.cd(argumentArr[0]));
                    else if (argumentArr.length > 1)
                        System.out.println("Too many arguments");
                }

                case "find" -> {
                    if (argumentArr.length == 0 || (argumentArr.length == 1 && argumentArr[0].equals("-h")))
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 2 && argumentArr[0].equals("-h") && argumentArr[1].equals("-h"))
                        System.out.println("Invalid argument");
                    else if (argumentArr.length == 2 && !argumentArr[0].equals("-h") && !argumentArr[1].equals("-h"))
                        System.out.println("Too many arguments");
                    else if (argumentArr.length > 2)
                        System.out.println("Too many arguments");
                    else {
                        boolean showHidden = false;
                        for (final String arg : argumentArr)
                            if (arg.equals("-h"))
                                showHidden = true;
                        CopyOnWriteArrayList<String> results;
                        if (showHidden)
                            results = workingDir.regexFilter(argumentArr[0], ListOption.SHOW_HIDDEN);
                        else
                            results = workingDir.regexFilter(argumentArr[0]);
                        if (results.size() == 0)
                            System.out.println("Not Found");
                        else
                            for (final String result : results)
                                System.out.println("Found " + result);
                    }
                }

                case "prop" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println(File.properties(argumentArr[0]));
                    else if (argumentArr.length > 1)
                        System.out.println("Too many arguments");
                }

                case "open" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println(File.open(argumentArr[0]));
                    else if (argumentArr.length > 1)
                        System.out.println("Too many arguments");
                }

                case "ls" -> {
                    CopyOnWriteArrayList<String> files = null, folders = null;
                    if (argumentArr.length > 1)
                        System.out.println("Too many arguments");
                    else if (argumentArr.length != 0 && !argumentArr[0].equals("-h"))
                        System.out.println("Invalid argument");
                    else if (argumentArr.length != 0 && argumentArr[0].equals("-h")) {
                        folders = workingDir.listFolders(ListOption.SHOW_HIDDEN);
                        files = workingDir.listFiles(ListOption.SHOW_HIDDEN);
                    } else {
                        folders = workingDir.listFolders();
                        files = workingDir.listFiles();
                    }
                    if (folders != null && folders.size() != 0) {
                        System.out.println("Folders:");
                        for (final String folderName : folders)
                            System.out.println(folderName);
                        System.out.println();
                    } else
                        System.out.println("No Folders here");
                    if (files != null && files.size() != 0) {
                        System.out.println("Files:");
                        for (final String fileName : files)
                            System.out.println(fileName);
                    } else
                        System.out.println("No Files here");
                }

                case "touch" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else
                        System.out.println(workingDir.createNewFile(argumentArr));
                }

                case "mkdir" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else
                        System.out.println(workingDir.create(argumentArr));
                }

                case "cp" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println("Insufficient Arguments");
                    else
                        System.out.println(workingDir.copy(argumentArr[argumentArr.length - 1],
                                Arrays.copyOfRange(argumentArr, 0, argumentArr.length - 1)));
                }

                case "slowcp" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println("Insufficient Arguments");
                    else
                        System.out.println(workingDir.nonAsyncCopy(argumentArr[argumentArr.length - 1],
                                Arrays.copyOfRange(argumentArr, 0, argumentArr.length - 1)));
                }

                case "mv" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println("Insufficient Arguments");
                    else
                        System.out.println(workingDir.move(argumentArr[argumentArr.length - 1],
                                Arrays.copyOfRange(argumentArr, 0, argumentArr.length - 1)));
                }

                case "rename" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else if (argumentArr.length == 1)
                        System.out.println("Insufficient Arguments");
                    else if (argumentArr.length > 2)
                        System.out.println("Too many Arguments");
                    else
                        System.out.println(workingDir.rename(argumentArr[0], argumentArr[1]));
                }

                case "rm" -> {
                    if (argumentArr.length == 0)
                        System.out.println("Missing Arguments");
                    else
                        System.out.println(workingDir.delete(argumentArr));

                }

                case "clear" -> {
                    if (prompt.equalsIgnoreCase("clear")) {
                        System.out.print("\033[H\033[2J"); // ANSI escape sequence to clear screen
                        System.out.flush(); // Flush the output buffer to ensure the screen is cleared immediately
                    }
                }
                case "exit" -> {
                    break outer;
                }
                case "help" -> {
                    showHelp();
                }
                default -> System.out.println("Invalid Choice");
            }
            System.out.println();
        }
        sc.close();
        workingDir.shutdownExecutorService();
    }
}
