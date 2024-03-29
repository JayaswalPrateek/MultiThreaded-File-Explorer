package core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Paths;

final class FileImpl implements File {
    private volatile String path, name;

    FileImpl() { // only used by FolderImpl to call default methods of Folder Interface
    }

    FileImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    FileImpl(final String newName, final FolderImpl obj) {
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
        final String path = FolderImpl.getInstance().getPath() + FolderImpl.getInstance().getName() + '/';
        final String[] pathsAndNames = Arrays.stream(names).map(name -> path + (destination.equals(".") ? "" : destination) + name).toArray(String[]::new);
        if (CriticalSectionHandler.isLocked(pathsAndNames))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(pathsAndNames);
        if (!destination.equals(".") && (!Files.exists(Paths.get(path + destination)) || !Files.isDirectory(Paths.get(path + destination))))
            return ErrorCode.DIR_NOT_FOUND;
        for (final String newFileNameWithPath : pathsAndNames) {
            if (DEBUG)
                System.out.println("CREATING " + newFileNameWithPath);
            try {
                Files.createFile(Paths.get(newFileNameWithPath));
            } catch (final UnsupportedOperationException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (final java.nio.file.FileAlreadyExistsException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                // return ErrorCode.FILE_ALREADY_EXISTS; // implies we replace existing
            } catch (final IOException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.IO_ERROR;
            } catch (final Exception e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.UNKOWN_ERROR;
            } finally {
                CriticalSectionHandler.unlock(newFileNameWithPath);
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode create(final String... names) {
        return create(".", names);
    }
}
