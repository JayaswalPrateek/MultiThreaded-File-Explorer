package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.awt.Desktop;

public final class FileImpl implements File {
    private String name, path;

    public FileImpl(final String name, final String path) {
        this.name = name;
        this.path = path + '/';
    }

    public FileImpl(final FileImpl obj) {
        this(obj.name, obj.path);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath() + getName();
    }

    public ErrorCode create(final String... names) {
        return create(".", names);
    }

    public ErrorCode create(final String destination, final String... names) {
        for (final String name : names)
            for (final char ch : name.toCharArray())
                if (ILLEGAL_CHARACTERS.contains(ch))
                    return ErrorCode.ILLEGAL_NAME;
        for (final String newFileName : names) {
            final String fullPath = (destination.equals(".") ? (path + name) : destination) + newFileName;
            Path pathToFile = Paths.get(fullPath);
            try {
                Files.createFile(pathToFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode copy(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    public ErrorCode copy(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    public ErrorCode delete(final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public ErrorCode delete(final String destination, final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public ErrorCode move(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    public ErrorCode move(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public ErrorCode open() {
        if (!Desktop.isDesktopSupported())
            return ErrorCode.OPERATION_NOT_SUPPORTED;

        Desktop desktop = Desktop.getDesktop();
        java.io.File file = new java.io.File(path + name);

        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ErrorCode.SUCCESS;
    }

    public ErrorCode properties() {
        Path p = Paths.get(path + name);
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
        java.io.File file = new java.io.File(path + name);
        System.out.println("Readable: " + file.canRead());
        System.out.println("Writable: " + file.canWrite());
        System.out.println("Executable: " + file.canExecute());

        return ErrorCode.SUCCESS;
    }
}
