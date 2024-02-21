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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

interface Entity extends Runnable {
    static final boolean DEBUG = true;
    static final Set<Character> ILLEGAL_CHARACTERS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList('/', '\\', ':', '*', '?', '"', '<', '>', '|')));

    final static class CriticalSectionHandler {
        private static final ConcurrentHashMap<Entity, ReentrantLock> lockedEntities = new ConcurrentHashMap<>();

        private static synchronized Entity[] namesToEntities(final String... names) {
            final Set<Entity> entitySetFromNames = Collections.synchronizedSet(new HashSet<>());
            for (final String name : names)
                for (final Entity entity : lockedEntities.keySet())
                    if (entity.getName().equals(name))
                        entitySetFromNames.add(entity);
            return entitySetFromNames.toArray(new Entity[0]);
        }

        static synchronized void lock(final Entity... entities) {
            for (final Entity entity : entities)
                lockedEntities.computeIfAbsent(entity, k -> new ReentrantLock()).lock();
            if (DEBUG)
                for (final Entity entity : entities)
                    System.out.println("LOCKED " + entity);
        }

        static synchronized void lock(final String... names) {
            lock(namesToEntities(names));
        }

        static synchronized void unlock(final Entity... entities) {
            for (final Entity entity : entities) {
                final ReentrantLock lock = lockedEntities.get(entity);
                if (lock != null && lock.isHeldByCurrentThread())
                    lock.unlock();
            }
            if (DEBUG)
                for (final Entity entity : entities)
                    System.out.println("UNLOCKED " + entity);
        }

        static synchronized void unlock(final String... names) {
            unlock(namesToEntities(names));
        }

        static synchronized boolean isLocked(final Entity... entities) {
            for (final Entity entity : entities) {
                final ReentrantLock lock = lockedEntities.get(entity);
                if (!(lock != null && lock.isLocked())) {
                    if (DEBUG)
                        System.out.println(entity + " NOT LOCKED");
                    return false;
                }
            }
            if (DEBUG)
                System.out.println("ALL ENTITIES LOCKED");
            return true;
        }

        static synchronized boolean isLocked(final String... names) {
            return isLocked(namesToEntities(names));
        }
    }

    @Override
    public boolean equals(final Object obj);

    @Override
    String toString();

    String getPath();

    String getName();

    void setPath(final String path);

    void setName(final String name);

    boolean doesExist();

    ErrorCode create(final String destination, final String... names);

    ErrorCode create(final String... names);

    default ErrorCode delete(final String destination, final String... names) { // for files and empty directories
        if (CriticalSectionHandler.isLocked(names))
            return ErrorCode.ENTITY_IS_LOCKED;
        for (final String name : names)
            if ((getPath() + getName()).startsWith(destination + name))
                return ErrorCode.OPERATION_NOT_SUPPORTED;
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
        if (CriticalSectionHandler.isLocked(obj))
            return ErrorCode.ENTITY_IS_LOCKED;
        if (DEBUG)
            System.out.println("MOVING " + obj.getPath() + obj.getName() + " to " + (destination == "." ? obj.getPath()
                    : destination) + newName);
        try {
            final Path sourcePath = Path.of(obj.getPath() + obj.getName());
            final Path targetPath = Path.of((destination == "." ? obj.getPath() : destination) + newName);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (final IOException e) {
            return ErrorCode.IO_ERROR;
        } catch (final Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        }
        obj.setPath(destination == "." ? obj.getPath() : destination);
        obj.setName(newName);
        return ErrorCode.SUCCESS;
    }

    default ErrorCode move(final String destination, final Entity obj) {
        return move(destination, obj, obj.getName());
    }

    default ErrorCode rename(final String newName, final Entity obj) {
        if (CriticalSectionHandler.isLocked(obj))
            return ErrorCode.ENTITY_IS_LOCKED;
        if (DEBUG)
            System.out.print("RENAMING BY ");
        return move(".", obj, newName);
    }
}