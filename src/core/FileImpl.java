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
        this.path = path.endsWith("/") ? path : path + '/';
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
            System.out.println("Checking if " + path + name + " exists");
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
                System.out.println("Creating " + destination + newFileName);
            final String fullPath = (destination.equals(".") ? path : destination) + newFileName;
            Path pathToFile = Paths.get(fullPath);
            try {
                Files.createFile(pathToFile);
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
        Path sourcePath = Paths.get(path + name);
        Path targetPath = Paths.get(destination + newName);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
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

    public ErrorCode open() {
        if (DEBUG)
            System.out.println("Opening " + path + name);
        if (!Desktop.isDesktopSupported())
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        final Desktop desktop = Desktop.getDesktop();
        final java.io.File file = new java.io.File(path + name);
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode properties() {
        if (DEBUG)
            System.out.println("Properties of " + path + name);
        final Path p = Paths.get(path + name);
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(p, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Size: " + attrs.size());
        System.out.println("Creation time: " + attrs.creationTime());
        System.out.println("Last access time: " + attrs.lastAccessTime());
        System.out.println("Last modified time: " + attrs.lastModifiedTime());
        final java.io.File file = new java.io.File(path + name);
        System.out.println("Readable: " + file.canRead());
        System.out.println("Writable: " + file.canWrite());
        System.out.println("Executable: " + file.canExecute());
        return ErrorCode.SUCCESS;
    }
}
