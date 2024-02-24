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
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public final class FolderImpl implements Folder {
    private volatile String path, name;

    private static final class Splitter {
        private final String path, name;

        Splitter(final String pathWithName) {
            path = pathWithName.substring(0, 1 + pathWithName.lastIndexOf('/'));
            name = pathWithName.substring(1 + pathWithName.lastIndexOf('/'));
            if (DEBUG)
                System.out.println("Splitting " + pathWithName + " into " + path + " and " + name);
        }

        String getPath() {
            return path;
        }

        String getName() {
            return name;
        }
    }

    private static final Splitter s = new Splitter(System.getProperty("user.home"));
    private static final FolderImpl singletonObj = new FolderImpl(s.getPath(), s.getName());

    public static FolderImpl getInstance() {
        return singletonObj;
    }

    private FolderImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    private FolderImpl(final String pathWithName) {
        this(pathWithName.substring(0, pathWithName.lastIndexOf('/' + 1)),
                pathWithName.substring(pathWithName.lastIndexOf('/' + 1)));
    }

    private FolderImpl(final FolderImpl obj) {
        this(obj.path, obj.name + "-copy");
        copy(".", name);
    }

    private FolderImpl(final String newName, final FolderImpl obj) {
        this(obj.getPath() + obj.getName(), newName);
    }

    public String getPath() {
        final String p = path;
        return p;
    }

    public String getName() {
        final String n = name;
        return n;
    }

    public void setPath(final String path) {
        CriticalSectionHandler.lock(this);
        this.path = path;
        CriticalSectionHandler.unlock(this);
    }

    public void setName(final String name) {
        CriticalSectionHandler.lock(this);
        this.name = name;
        CriticalSectionHandler.unlock(this);
    }

    public boolean doesExist() {
        if (DEBUG)
            System.out.println("CHECKING IF " + path + name + " EXISTS: ");
        CriticalSectionHandler.lock(this);
        final boolean result = Files.exists(Path.of(path, name)) && Files.isDirectory(Path.of(path, name));
        CriticalSectionHandler.unlock(this);
        if (DEBUG)
            System.out.println((result ? "" : "NOT ") + "FOUND");
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        CriticalSectionHandler.lock(this);
        boolean result;
        if (this == obj)
            result = true;
        else if (obj == null || getClass() != obj.getClass())
            result = false;
        else {
            result = this.toString().equals(obj.toString());
        }
        CriticalSectionHandler.unlock(this);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name);
    }

    @Override
    public String toString() {
        return getPath() + getName();
    }

    public synchronized ErrorCode create(final String destination, final String... names) {
        for (final String name : names)
            for (final char ch : name.toCharArray())
                if (ILLEGAL_CHARACTERS.contains(ch))
                    return ErrorCode.ILLEGAL_NAME;
        for (final String newFolderName : names) {
            if (DEBUG)
                System.out.println(
                        "CREATING " + (destination.equals(".") ? (path + name + '/') : destination) + newFolderName);
            try {
                Files.createDirectories(
                        Paths.get((destination.equals(".") ? (path + name + '/') : destination) + newFolderName));
            } catch (final UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (final java.nio.file.FileAlreadyExistsException e) {
                return ErrorCode.FILE_ALREADY_EXISTS;
            } catch (final IOException e) {
                return ErrorCode.IO_ERROR;
            } catch (final Exception e) {
                return ErrorCode.UNKOWN_ERROR;
            }
        }
        return ErrorCode.SUCCESS;
    }

    public synchronized ErrorCode create(final String... names) {
        return create(".", names);
    }

    public synchronized ErrorCode createNewFile(final String... newFileNames) {
        return new FileImpl(newFileNames[0], this).create(newFileNames);
    }

    public ErrorCode copy(final String destination, final String... namesWithoutPaths) {
        if (CriticalSectionHandler.isLocked(this))
            return ErrorCode.ENTITY_IS_LOCKED;
        for (final String name : namesWithoutPaths)
            if (!getNameFromPathAndName(listFiles()).contains(name)
                    && !getNameFromPathAndName(listFolders()).contains(name))
                return ErrorCode.ENTITY_NOT_FOUND;
        for (final String name : namesWithoutPaths) {
            if (DEBUG)
                System.out.println(
                        "COPYING " + getPath() + name + " TO " + (destination.equals(".") ? getPath() : destination) +
                                name);
            try {
                if (getNameFromPathAndName(listFiles()).contains(name)) {
                    Files.copy(Paths.get(getPath() + name), Paths.get(
                            (destination.equals(".") ? getPath() : destination) + name),
                            StandardCopyOption.REPLACE_EXISTING);
                } else {
                    final Path sourcePath = Paths.get(getPath() + name);
                    final Path targetPath = Paths.get((destination.equals(".") ? getPath() : destination) + name);
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
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                                throws IOException {
                            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)),
                                    StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            } catch (final UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (final java.nio.file.FileAlreadyExistsException e) {
                return ErrorCode.FILE_ALREADY_EXISTS;
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final Exception e) {
                return ErrorCode.UNKOWN_ERROR;
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode move(final String srcPath, final String srcName, final String destPath, final String destName) {
        if (CriticalSectionHandler.isLocked(this))
            return ErrorCode.ENTITY_IS_LOCKED;
        final String srcFileLocation = this.getPath() + this.getName() + "/" + (srcPath.equals(".") ? "" : srcPath)
                + (srcName.equals(".") ? "" : srcName);
        final String destFileLocation = this.getPath() + this.getName() + "/" + (destPath.equals(".") ? "" : destPath)
                + (destName.equals(".") ? "" : destName);

        if (!Files.exists(Paths.get(srcFileLocation)))
            return ErrorCode.ENTITY_NOT_FOUND;
        if (DEBUG)
            System.out.println("MOVING " + srcFileLocation + " TO " + destFileLocation);
        try {
            Files.move(Path.of(srcFileLocation), Path.of(destFileLocation),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
        } catch (Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode move(final String destination, final String... names) {
        for (final String name : names) {
            final ErrorCode result = move(".", name, destination, name);
            if (result != ErrorCode.SUCCESS)
                return result;
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode rename(final String oldName, final String newName) {
        return move(".", oldName, ".", newName);
    }

    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public CopyOnWriteArrayList<String> listFiles(final ListOption opt) {
        final CopyOnWriteArrayList<String> files = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name))) {
            for (final Path path : stream)
                if (!Files.isDirectory(path))
                    if (opt == ListOption.SHOW_HIDDEN || !Files.isHidden(path))
                        files.add(path.toString());
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        }
        return files;
    }

    public CopyOnWriteArrayList<String> listFiles() {
        return listFiles(ListOption.NONE);
    }

    public CopyOnWriteArrayList<String> listFolders(final ListOption opt) {
        final CopyOnWriteArrayList<String> folders = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name),
                Files::isDirectory)) {
            for (final Path path : stream)
                if (opt == ListOption.SHOW_HIDDEN || !Files.isHidden(path))
                    folders.add(path.toString());
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        }
        return folders;
    }

    public CopyOnWriteArrayList<String> listFolders() {
        return listFolders(ListOption.NONE);
    }

    private CopyOnWriteArrayList<String> getNameFromPathAndName(final CopyOnWriteArrayList<String> entityList) {
        for (int i = 0; i < entityList.size(); i++) {
            final String fullPath = entityList.get(i);
            String nameOnly = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (nameOnly.startsWith("."))
                nameOnly = nameOnly.substring(1 + nameOnly.indexOf('.'));
            entityList.set(i, nameOnly);
        }
        return entityList;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString, final ListOption opt) {
        final CopyOnWriteArrayList<String> Filtered = new CopyOnWriteArrayList<String>();
        final Pattern pattern = Pattern.compile(patternString);
        CriticalSectionHandler.lock(this);
        for (final String candidateFile : getNameFromPathAndName(listFiles(opt)))
            if (pattern.matcher(candidateFile).matches())
                Filtered.add(candidateFile);
        for (final String candidateFolder : getNameFromPathAndName(listFolders(opt)))
            if (pattern.matcher(candidateFolder).matches())
                Filtered.add(candidateFolder);
        CriticalSectionHandler.unlock(this);
        return Filtered;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString) {
        return regexFilter(patternString, ListOption.NONE);
    }

    public ErrorCode stepIn(final String target) {
        if (DEBUG)
            System.out.println("STEPPING IN FROM PATH=" + path + " NAME=" + name + " TO " + target);
        if (!getNameFromPathAndName(listFolders()).contains(target))
            return ErrorCode.DIR_NOT_FOUND;
        path += name + '/';
        name = target;
        return ErrorCode.SUCCESS;
    }

    public ErrorCode stepOut() {
        if (DEBUG)
            System.out.print("STEPPING OUT OF PATH=" + path + " NAME=" + name);
        if (path.equals("/"))
            return ErrorCode.DIR_NOT_FOUND;
        final int lastSlash = path.lastIndexOf('/');
        final int secondLastIndex = path.lastIndexOf('/', lastSlash - 1);
        name = path.substring(1 + secondLastIndex, lastSlash);
        path = path.substring(0, secondLastIndex + 1);
        if (DEBUG)
            System.out.println(" TO PATH=" + path + " NAME=" + name);
        return ErrorCode.SUCCESS;
    }

    public ErrorCode cd(final String destination) { // doesnt handle a new absolute path and ~
        if (DEBUG)
            System.out.println("CHANGE DIR TO " + destination);
        for (final String segment : destination.split("/"))
            if (segment.equals("."))
                continue;
            else if (segment.equals("..") && stepOut() == ErrorCode.DIR_NOT_FOUND)
                return ErrorCode.DIR_NOT_FOUND;
            else if (stepIn(segment) == ErrorCode.DIR_NOT_FOUND)
                return ErrorCode.DIR_NOT_FOUND;
        return ErrorCode.SUCCESS;
    }
}