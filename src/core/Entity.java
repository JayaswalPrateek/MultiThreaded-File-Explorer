package core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

interface Entity extends Runnable {
    boolean DEBUG = true;

    static class criticalSectionHandler {
        private static final Set<Entity> lockedEntities = Collections.synchronizedSet(new HashSet<>());

        private static synchronized boolean lock(final Entity... entities) {
            boolean successTracker = true;
            for (final Entity entity : entities)
                successTracker = successTracker && lockedEntities.add(entity);
            if (!successTracker && DEBUG)
                System.out.println("Cannot recover from partial locking");
            if (!successTracker)
                System.exit(1);
            return successTracker;
        }

        private static synchronized boolean unlock(final Entity... entities) {
            boolean successTracker = true;
            for (final Entity entity : entities)
                successTracker = successTracker && lockedEntities.remove(entity);
            if (!successTracker && DEBUG)
                System.out.println("Cannot recover from partial unlocking");
            if (!successTracker)
                System.exit(1);
            return successTracker;
        }

        public static synchronized void getLockedEntities() {
            if (!DEBUG)
                return;
            for (final Entity entity : lockedEntities)
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

interface File extends Entity {
    ErrorCode open();

    ErrorCode properties();
}

interface Folder extends Entity {
    ErrorCode list();

    ErrorCode regexFilter(final String pattern);

    ErrorCode stepIn(final String target);

    ErrorCode stepOut();
}