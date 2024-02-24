package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

final class FileImpl implements File {
    private volatile String path, name;

    public FileImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
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
        boolean result;
        if (this == obj)
            result = true;
        else if (obj == null || getClass() != obj.getClass())
            result = false;
        else
            result = this.toString().equals(obj.toString());
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

    public ErrorCode create(final String destination, final String... names) {
        for (final String name : names)
            for (final char ch : name.toCharArray())
                if (ILLEGAL_CHARACTERS.contains(ch))
                    return ErrorCode.ILLEGAL_NAME;
        final String[] pathsAndNames = Arrays.stream(names)
                .map(name -> (destination.equals(".") ? path : destination) + name).toArray(String[]::new);
        if (CriticalSectionHandler.isLocked(pathsAndNames))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(pathsAndNames);
        for (final String newFileName : names) {
            if (DEBUG)
                System.out.println("CREATING " + (destination.equals(".") ? path : destination) + newFileName);
            try {
                Files.createFile(Paths.get((destination.equals(".") ? path : destination) + newFileName));
            } catch (final UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (final java.nio.file.FileAlreadyExistsException e) {
                // return ErrorCode.FILE_ALREADY_EXISTS; // implies we replace existing
            } catch (final IOException e) {
                return ErrorCode.IO_ERROR;
            } catch (final Exception e) {
                return ErrorCode.UNKOWN_ERROR;
            } finally {
                CriticalSectionHandler.unlock(pathsAndNames);
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
