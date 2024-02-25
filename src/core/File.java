package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.awt.Desktop;

public interface File extends Entity {
    static ErrorCode open(final String name) {
        final String path = FolderImpl.getInstance().getPath() + FolderImpl.getInstance().getName() + '/';
        if (DEBUG)
            System.out.println("OPENING " + path + name);
        if (!Desktop.isDesktopSupported())
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        if (!Files.exists(Path.of(path, name)))
            return ErrorCode.FILE_NOT_FOUND;
        if (CriticalSectionHandler.isLocked(path + name))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(path + name);
        try {
            Desktop.getDesktop().open(new java.io.File(path + name));
        } catch (final IOException e) {
            return ErrorCode.IO_ERROR;
        } catch (final Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        } finally {
            CriticalSectionHandler.unlock(path + name);
        }
        return ErrorCode.SUCCESS;
    }

    static ErrorCode open(final String... names) {
        final String path = FolderImpl.getInstance().getPath() + FolderImpl.getInstance().getName() + '/';
        for (final String name : names)
            if (!Files.exists(Path.of(path, name)))
                return ErrorCode.FILE_NOT_FOUND;
        for (final String name : names) {
            final ErrorCode result = open(name);
            if (result != ErrorCode.SUCCESS)
                return result;
        }
        return ErrorCode.SUCCESS;
    }

    static ErrorCode properties(final String name) {
        final String path = FolderImpl.getInstance().getPath() + FolderImpl.getInstance().getName() + '/';
        if (DEBUG)
            System.out.println("PROPERTIES OF " + path + name);
        if (!Files.exists(Path.of(path, name)))
            return ErrorCode.FILE_NOT_FOUND;
        if (CriticalSectionHandler.isLocked(path + name))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(path + name);
        try {
            final BasicFileAttributes attrs = Files.readAttributes(Paths.get(path + name), BasicFileAttributes.class);
            final java.io.File file = new java.io.File(path + name);
            System.out.println("Size: " + attrs.size());
            System.out.println("Creation time: " + attrs.creationTime());
            System.out.println("Last access time: " + attrs.lastAccessTime());
            System.out.println("Last modified time: " + attrs.lastModifiedTime());
            System.out.println("Readable: " + file.canRead());
            System.out.println("Writable: " + file.canWrite());
            System.out.println("Executable: " + file.canExecute());
        } catch (final UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (final IOException e) {
            return ErrorCode.IO_ERROR;
        } catch (final Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        } finally {
            CriticalSectionHandler.unlock(path + name);
        }
        return ErrorCode.SUCCESS;
    }
}
