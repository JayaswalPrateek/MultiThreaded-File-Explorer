package core;

import java.io.IOException;
import java.nio.file.Files;
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

    static class CriticalSectionHandler {
        private static final Set<Entity> lockedFiles = Collections.synchronizedSet(new HashSet<>());
        private static final Set<Entity> lockedFolders = Collections.synchronizedSet(new HashSet<>());

        private static synchronized boolean lock(final Entity... entities) {
            boolean allLockedSuccessfully = true;
            for (final Entity entity : entities)
                if (entity instanceof File)
                    allLockedSuccessfully = allLockedSuccessfully && lockedFiles.add(entity);
                else
                    allLockedSuccessfully = allLockedSuccessfully && lockedFolders.add(entity);
            if (DEBUG)
                if (allLockedSuccessfully)
                    for (final Entity entity : entities)
                        System.out.println("Successfully locked: " + entity);
                else
                    System.out.println("Cannot recover from partial locking");
            if (!allLockedSuccessfully)
                System.exit(1);
            return allLockedSuccessfully;
        }

        private static synchronized boolean unlock(final Entity... entities) {
            boolean allUnlockedSuccessfully = true;
            for (final Entity entity : entities)
                if (entity instanceof File)
                    allUnlockedSuccessfully = allUnlockedSuccessfully && lockedFiles.remove(entity);
                else
                    allUnlockedSuccessfully = allUnlockedSuccessfully && lockedFolders.remove(entity);
            if (DEBUG)
                if (allUnlockedSuccessfully)
                    for (final Entity entity : entities)
                        System.out.println("Successfully unlocked: " + entity);
                else
                    System.out.println("Cannot recover from partial unlocking");
            if (!allUnlockedSuccessfully)
                System.exit(1);
            return allUnlockedSuccessfully;
        }

        private static synchronized boolean isLocked(final Entity... entities) {
            for (final Entity entity : entities)
                if (entity instanceof File && lockedFiles.contains(entity))
                    return true;
                else if (entity instanceof Folder && lockedFolders.contains(entity))
                    return true;
            if (DEBUG)
                for (final Entity entity : entities)
                    System.out.println(entity + " not locked");
            return false;
        }

        public static synchronized void getLockedEntities() {
            if (!DEBUG)
                return;
            for (final Entity entity : lockedFiles)
                System.out.println(entity);
            for (final Entity entity : lockedFolders)
                System.out.println(entity);
        }
    }

    String getPath();

    String getName();

    boolean doesExist();

    ErrorCode create(final String destination, final String... names);

    ErrorCode create(final String... names);

    default ErrorCode delete(final String destination, final String... names) { // for files and empty directories
        for (final String name : names) {
            if (DEBUG)
                System.out.println("Deleting " + destination + name);
            try {
                Files.delete(Paths.get(name));
            } catch (IOException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
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
            System.out.println("Moving " + obj.getPath() + obj.getName() + " to " + destination + newName);
        try {
            Path sourcePath = Path.of(obj.getPath() + obj.getName());
            Path targetPath = Path.of((destination == "." ? obj.getPath() : destination) + newName);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        }
        return ErrorCode.SUCCESS;
    }

    default ErrorCode move(final String destination, final Entity obj) {
        return move(destination, obj, obj.getName());
    }

    default ErrorCode rename(final String newName, final Entity obj) {
        if (DEBUG)
            System.out.print("(Renaming)");
        return move(".", obj, newName);
    }
}