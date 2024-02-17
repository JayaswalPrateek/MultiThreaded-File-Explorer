package core;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FolderImpl implements Folder {
    private volatile String path, name;

    public FolderImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : path + '/';
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    public FolderImpl(final FolderImpl obj) {
        this(obj.path, obj.name + "-copy");
        copy(".", name);
    }

    public FolderImpl(final String newName, final FolderImpl obj) {
        this(obj.getPath() + obj.getName(), newName);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean doesExist() {
        if (DEBUG)
            System.out.println("Checking if " + path + name + " exists");
        return Files.exists(Path.of(path, name)) && Files.isDirectory(Path.of(path, name));
    }

    @Override
    public String toString() {
        return getPath() + getName();
    }

    public ErrorCode create(final String destination, final String... names) {
        for (final String name : names)
            for (final char ch : name.toCharArray())
                if (ILLEGAL_CHARACTERS.contains(ch))
                    return ErrorCode.ILLEGAL_NAME;
        for (final String newFolderName : names) {
            if (DEBUG)
                System.out.println("Creating " + destination + newFolderName);
            final String fullPath = (destination.equals(".") ? (path + name + '/') : destination) + newFolderName;
            Path pathToFolder = Paths.get(fullPath);
            try {
                Files.createDirectories(pathToFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode create(final String... names) {
        return create(".", names);
    }

    public ErrorCode copy(final String destination, final String newName) {
        if (DEBUG)
            System.out.println("Copying " + path + name + " to " + destination + newName);
        final Path sourcePath = Paths.get(path + name);
        final Path targetPath = Paths.get(destination + newName);
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                        throws IOException {
                    final Path targetDirPath = targetPath.resolve(sourcePath.relativize(dir));
                    if (!Files.exists(targetDirPath))
                        Files.createDirectories(targetDirPath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, targetPath.resolve(sourcePath.relativize(file)),
                            StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode copy(final String destination) {
        return copy(destination, name);
    }

    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public CopyOnWriteArrayList<String> listFiles() {
        final CopyOnWriteArrayList<String> files = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name))) {
            for (final Path path : stream)
                if (!Files.isDirectory(path))
                    files.add(path.toString());
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }
        return files;
    }

    public CopyOnWriteArrayList<String> listFolders() {
        final CopyOnWriteArrayList<String> folders = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name),
                Files::isDirectory)) {
            for (final Path path : stream)
                folders.add(path.toString());
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }
        return folders;
    }

    private CopyOnWriteArrayList<String> getNameFromPathAndName(final CopyOnWriteArrayList<String> entityList) {
        for (int i = 0; i < entityList.size(); i++) {
            final String fullPath = entityList.get(i);
            String nameOnly = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (nameOnly.startsWith("."))
                nameOnly = nameOnly.substring(1 + nameOnly.indexOf('.'));
            entityList.set(i, nameOnly);
            if (DEBUG)
                System.out.println("Name of Entity in " + fullPath + " is " + nameOnly);
        }
        return entityList;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString) {
        final CopyOnWriteArrayList<String> Files = getNameFromPathAndName(listFiles());
        final CopyOnWriteArrayList<String> Folders = getNameFromPathAndName(listFolders());

        final CopyOnWriteArrayList<String> Filtered = new CopyOnWriteArrayList<String>();
        final Pattern pattern = Pattern.compile(patternString);
        for (final String candidateFile : Files) {
            final Matcher matcher = pattern.matcher(candidateFile);
            final boolean matchFound = matcher.matches();
            if (matchFound)
                Filtered.add(candidateFile);
        }
        for (final String candidateFolder : Folders) {
            final Matcher matcher = pattern.matcher(candidateFolder);
            final boolean matchFound = matcher.matches();
            if (matchFound)
                Filtered.add(candidateFolder);
        }
        return Filtered;
    }

    public ErrorCode stepIn(final String target) {
        if (DEBUG)
            System.out.println("Stepping in from path=" + path + " name=" + name + " to " + target);
        final CopyOnWriteArrayList<String> Folders = getNameFromPathAndName(listFolders());
        if (!Folders.contains(target))
            return ErrorCode.FOLDER_NOT_FOUND;
        path += name + '/';
        name = target;
        return ErrorCode.SUCCESS;
    }

    public ErrorCode stepOut() {
        if (DEBUG)
            System.out.println("Stepping out path=" + path + " name=" + name);
        if (path.equals("/"))
            return ErrorCode.FOLDER_NOT_FOUND;
        final int lastSlash = path.lastIndexOf('/');
        final int secondLastIndex = path.lastIndexOf('/', lastSlash - 1);
        name = path.substring(1 + secondLastIndex, lastSlash);
        path = path.substring(0, secondLastIndex + 1);
        return ErrorCode.SUCCESS;
    }

    public ErrorCode cd(final String destination) { // doesnt handle a new absolute path and ~
        if (DEBUG)
            System.out.println("cding into " + destination);
        final String[] segments = destination.split("/");
        for (final String segment : segments)
            if (segment.equals("."))
                continue;
            else if (segment.equals("..") && stepOut() == ErrorCode.FOLDER_NOT_FOUND)
                return ErrorCode.FOLDER_NOT_FOUND;
            else if (stepIn(segment) == ErrorCode.FOLDER_NOT_FOUND)
                return ErrorCode.FOLDER_NOT_FOUND;
        return ErrorCode.SUCCESS;
    }
}