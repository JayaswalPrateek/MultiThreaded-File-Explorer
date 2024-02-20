package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.awt.Desktop;

public final class FileImpl implements File {
    private volatile String path, name;

    public FileImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    public FileImpl(final FileImpl obj) {
        this(obj.path, obj.name + "-copy");
        copy(".", name);
    }

    public FileImpl(final String newName, final FolderImpl obj) {
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
            System.out.println("CHECKING IF " + path + name + " EXISTS");
        return Files.exists(Path.of(path, name));
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
        for (final String newFileName : names) {
            if (DEBUG)
                System.out.println("CREATING " + (destination.equals(".") ? path : destination) + newFileName);
            final String fullPath = (destination.equals(".") ? path : destination) + newFileName;
            final Path pathToFile = Paths.get(fullPath);
            try {
                Files.createFile(pathToFile);
            } catch (UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (IOException e) {
                return ErrorCode.IO_ERROR;
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode create(final String... names) {
        return create(".", names);
    }

    public ErrorCode copy(final String destination, final String newName) {
        if (DEBUG)
            System.out.println("COPYING " + path + name + " TO " + destination + newName);
        final Path sourcePath = Paths.get(path + name);
        final Path targetPath = Paths.get(destination + newName);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
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

    public ErrorCode open() {
        if (DEBUG)
            System.out.println("OPENING " + path + name);
        if (!Desktop.isDesktopSupported())
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        if (!doesExist())
            return ErrorCode.FILE_NOT_FOUND;
        if (CriticalSectionHandler.isLocked(this))
            return ErrorCode.FILE_IS_LOCKED;
        final Desktop desktop = Desktop.getDesktop();
        final java.io.File file = new java.io.File(path + name);
        try {
            desktop.open(file);
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode properties() {
        if (DEBUG)
            System.out.println("PROPERTIES OF " + path + name);
        if (CriticalSectionHandler.isLocked(this))
            return ErrorCode.FILE_IS_LOCKED;
        final Path p = Paths.get(path + name);
        try {
            final BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
            final java.io.File file = new java.io.File(path + name);
            System.out.println("Size: " + attrs.size());
            System.out.println("Creation time: " + attrs.creationTime());
            System.out.println("Last access time: " + attrs.lastAccessTime());
            System.out.println("Last modified time: " + attrs.lastModifiedTime());
            System.out.println("Readable: " + file.canRead());
            System.out.println("Writable: " + file.canWrite());
            System.out.println("Executable: " + file.canExecute());
        } catch (UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
        }
        return ErrorCode.SUCCESS;
    }
}
