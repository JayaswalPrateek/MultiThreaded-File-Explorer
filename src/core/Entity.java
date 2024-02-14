package core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

interface Entity extends Runnable {
    static boolean DEBUG = true;

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
            if (!allLockedSuccessfully && DEBUG)
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
            if (!allUnlockedSuccessfully && DEBUG)
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

    String getName();

    String getPath();

    private void setName(final String name) {
    }

    private void setPath(final String path) {
    }

    ErrorCode create(final String... names);

    ErrorCode create(final String destination, final String... names);

    ErrorCode delete(final String... names);

    ErrorCode delete(final String destination, final String... names);

    ErrorCode copy(final String destination);

    ErrorCode copy(final String destination, final String... newName);

    ErrorCode move(final String destination);

    ErrorCode move(final String destination, final String... newName);

    ErrorCode rename(final String newName);

}