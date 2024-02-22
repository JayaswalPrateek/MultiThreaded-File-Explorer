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
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    public FolderImpl(final String pathWithName) {
        this(pathWithName.substring(0, pathWithName.lastIndexOf('/' + 1)),
                pathWithName.substring(pathWithName.lastIndexOf('/' + 1)));
    }

    public FolderImpl(final FolderImpl obj) {
        this(obj.path, obj.name + "-copy");
        copy(".", name);
    }

    public FolderImpl(final String newName, final FolderImpl obj) {
        this(obj.getPath() + obj.getName(), newName);
    }

    public String getPath() {
        CriticalSectionHandler.lock(this);
        final String p = path;
        CriticalSectionHandler.unlock(this);
        return p;
    }

    public String getName() {
        CriticalSectionHandler.lock(this);
        final String n = name;
        CriticalSectionHandler.unlock(this);
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
            System.out.println("CHECKING IF " + path + name + " EXISTS");
        CriticalSectionHandler.lock(this);
        final boolean result = Files.exists(Path.of(path, name)) && Files.isDirectory(Path.of(path, name));
        CriticalSectionHandler.unlock(this);
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
            final Entity other = (Entity) obj;
            result = (this.getPath() + this.getName()).equals(other.getPath() + other.getName());
        }
        CriticalSectionHandler.unlock(this);
        return result;
    }

    @Override
    public String toString() {
        CriticalSectionHandler.lock(this);
        final String resultString = getPath() + getName();
        CriticalSectionHandler.unlock(this);
        return resultString;
    }

    public ErrorCode create(final String destination, final String... names) {
        for (final String name : names)
            for (final char ch : name.toCharArray())
                if (ILLEGAL_CHARACTERS.contains(ch))
                    return ErrorCode.ILLEGAL_NAME;
        for (final String newFolderName : names) {
            if (DEBUG)
                System.out.println(
                        "CREATING " + (destination.equals(".") ? (path + name + '/') : destination) + newFolderName);
            final String fullPath = (destination.equals(".") ? (path + name + '/') : destination) + newFolderName;
            final Path pathToFolder = Paths.get(fullPath);
            try {
                Files.createDirectories(pathToFolder);
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

    public ErrorCode create(final String... names) {
        return create(".", names);
    }

    public ErrorCode copy(final String destination, final String newName) {
        if (CriticalSectionHandler.isLocked(this))
            return ErrorCode.ENTITY_IS_LOCKED;
        if (DEBUG)
            System.out.println("COPYING " + path + name + " TO " + destination + newName);
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
        } catch (final UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (final java.nio.file.FileAlreadyExistsException e) {
            return ErrorCode.FILE_ALREADY_EXISTS;
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            return ErrorCode.UNKOWN_ERROR;
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
            CriticalSectionHandler.lock(this);
            for (final Path path : stream)
                if (!Files.isDirectory(path))
                    files.add(path.toString());
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        } finally {
            CriticalSectionHandler.unlock(this);
        }
        return files;
    }

    public CopyOnWriteArrayList<String> listFolders() {
        final CopyOnWriteArrayList<String> folders = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name),
                Files::isDirectory)) {
            CriticalSectionHandler.lock(this);
            for (final Path path : stream)
                folders.add(path.toString());
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        } finally {
            CriticalSectionHandler.unlock(this);
        }
        return folders;
    }

    private CopyOnWriteArrayList<String> getNameFromPathAndName(final CopyOnWriteArrayList<String> entityList) {
        for (int i = 0; i < entityList.size(); i++) {
            // CriticalSectionHandler.lock(entityList.toArray()[i]);
            final String fullPath = entityList.get(i);
            String nameOnly = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (nameOnly.startsWith("."))
                nameOnly = nameOnly.substring(1 + nameOnly.indexOf('.'));
            entityList.set(i, nameOnly);
            // CriticalSectionHandler.lock(entityList.toArray()[i]);
            if (DEBUG)
                System.out.println("NAME OF THE ENTITY IN " + fullPath + " IS " + nameOnly);
        }
        return entityList;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString) {
        final CopyOnWriteArrayList<String> Files = getNameFromPathAndName(listFiles());
        final CopyOnWriteArrayList<String> Folders = getNameFromPathAndName(listFolders());

        final CopyOnWriteArrayList<String> Filtered = new CopyOnWriteArrayList<String>();
        final Pattern pattern = Pattern.compile(patternString);
        CriticalSectionHandler.lock(this);
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
        CriticalSectionHandler.unlock(this);
        return Filtered;
    }

    public ErrorCode stepIn(final String target) {
        if (DEBUG)
            System.out.println("STEPPING IN FROM PATH=" + path + " NAME=" + name + " TO " + target);
        final CopyOnWriteArrayList<String> Folders = getNameFromPathAndName(listFolders());
        if (!Folders.contains(target))
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
            System.out.println("CHANGE DIR TO" + destination);
        final String[] segments = destination.split("/");
        for (final String segment : segments)
            if (segment.equals("."))
                continue;
            else if (segment.equals("..") && stepOut() == ErrorCode.DIR_NOT_FOUND)
                return ErrorCode.DIR_NOT_FOUND;
            else if (stepIn(segment) == ErrorCode.DIR_NOT_FOUND)
                return ErrorCode.DIR_NOT_FOUND;
        return ErrorCode.SUCCESS;
    }
}