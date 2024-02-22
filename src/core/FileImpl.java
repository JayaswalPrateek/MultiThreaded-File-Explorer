package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class FileImpl implements File {
    private volatile String path, name;

    public FileImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    public FileImpl(final FileImpl obj) {
        this(obj.path, obj.name + "-copy");
        new FolderImpl(obj.getPath()).copy(".", name);
    }

    public FileImpl(final String newName, final FolderImpl obj) {
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
        final boolean result = Files.exists(Path.of(path, name));
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
        for (final String newFileName : names) {
            if (DEBUG)
                System.out.println("CREATING " + (destination.equals(".") ? path : destination) + newFileName);
            final String fullPath = (destination.equals(".") ? path : destination) + newFileName;
            final Path pathToFile = Paths.get(fullPath);
            try {
                Files.createFile(pathToFile);
            } catch (final UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
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

    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
}
