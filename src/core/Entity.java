package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

interface Entity extends Runnable {
    static final boolean DEBUG = true;
    static final Set<Character> ILLEGAL_CHARACTERS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList('/', '\\', ':', '*', '?', '"', '<', '>', '|')));

    final static class CriticalSectionHandler {
        private static volatile Set<Entity> lockedFiles = Collections.synchronizedSet(new HashSet<>());
        private static volatile Set<Entity> lockedFolders = Collections.synchronizedSet(new HashSet<>());

        static synchronized void lock(final Entity... entities) {
            boolean allLockedSuccessfully = true;
            for (final Entity entity : entities)
                if (entity instanceof File)
                    allLockedSuccessfully = allLockedSuccessfully && lockedFiles.add(entity);
                else
                    allLockedSuccessfully = allLockedSuccessfully && lockedFolders.add(entity);
            if (DEBUG)
                if (allLockedSuccessfully)
                    for (final Entity entity : entities)
                        System.out.println("LOCKED " + entity);
                else
                    System.out.println("FATAL: CANNOT RECOVER FROM PARTIAL LOCKING");
            if (!allLockedSuccessfully)
                System.exit(1);
        }

        static synchronized void unlock(final Entity... entities) {
            boolean allUnlockedSuccessfully = true;
            for (final Entity entity : entities)
                if (entity instanceof File)
                    allUnlockedSuccessfully = allUnlockedSuccessfully && lockedFiles.remove(entity);
                else
                    allUnlockedSuccessfully = allUnlockedSuccessfully && lockedFolders.remove(entity);
            if (DEBUG)
                if (allUnlockedSuccessfully)
                    for (final Entity entity : entities)
                        System.out.println("UNLOCKED " + entity);
                else
                    System.out.println("FATAL: CANNOT RECOVER FROM PARTIAL UNLOCKING");
            if (!allUnlockedSuccessfully)
                System.exit(1);
        }

        static synchronized boolean isLocked(final Entity... entities) {
            boolean lockStatus = true;
            for (final Entity entity : entities)
                if ((entity instanceof File && !lockedFiles.contains(entity))
                        || (entity instanceof Folder && !lockedFolders.contains(entity))) {
                    lockStatus = false;
                    if (DEBUG)
                        System.out.println(entity + " NOT LOCKED");
                    break;
                }
            if (DEBUG && lockStatus)
                System.out.println("ALL ENTITIES LOCKED");
            return lockStatus;
        }
    }

    @Override
    String toString();

    String getPath();

    String getName();

    boolean doesExist();

    ErrorCode create(final String destination, final String... names);

    ErrorCode create(final String... names);

    default ErrorCode delete(final String destination, final String... names) { // for files and empty directories
        for (final String name : names) {
            if (DEBUG)
                System.out.println("DELETING " + destination + name);
            try {
                Files.delete(Paths.get(name));
            } catch (final NoSuchFileException e) {
                return ErrorCode.FILE_NOT_FOUND;
            } catch (final java.nio.file.DirectoryNotEmptyException e) {
                return ErrorCode.DIR_NOT_EMPTY;
            } catch (final IOException e) {
                return ErrorCode.IO_ERROR;
            } catch (final Exception e) {
                return ErrorCode.UNKOWN_ERROR;
            }
        }
        return ErrorCode.SUCCESS;
    }

    default ErrorCode delete(final Entity obj, final String... names) {
        return delete(obj.getPath(), names);
    }

    ErrorCode copy(final String destination, final String newName);

    ErrorCode copy(final String destination);

    default ErrorCode move(final String destination, final Entity obj, final String newName) {
        if (DEBUG)
            System.out.println("MOVING " + obj.getPath() + obj.getName() + " to " + (destination == "." ? obj.getPath()
                    : destination) + newName);
        try {
            final Path sourcePath = Path.of(obj.getPath() + obj.getName());
            final Path targetPath = Path.of((destination == "." ? obj.getPath() : destination) + newName);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
        }
        return ErrorCode.SUCCESS;
    }

    default ErrorCode move(final String destination, final Entity obj) {
        return move(destination, obj, obj.getName());
    }

    default ErrorCode rename(final String newName, final Entity obj) {
        if (DEBUG)
            System.out.print("RENAMING BY ");
        return move(".", obj, newName);
    }
}