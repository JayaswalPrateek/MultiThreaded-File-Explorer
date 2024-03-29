package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

interface Entity {
    static final boolean DEBUG = false;
    static final Set<Character> ILLEGAL_CHARACTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList('/', '\\', ':', '*', '?', '"', '<', '>', '|')));

    final static class CriticalSectionHandler {
        private static final ConcurrentHashMap<String, ReentrantLock> lockedEntities = new ConcurrentHashMap<>();

        static synchronized void lock(final String... pathsAndNames) {
            for (final String pathAndName : pathsAndNames) {
                if (DEBUG)
                    System.out.println("TRYING TO LOCK " + pathAndName);
                lockedEntities.computeIfAbsent(pathAndName, k -> new ReentrantLock()).lock();
                if (DEBUG)
                    System.out.println("LOCKED " + pathAndName);
            }
        }

        static synchronized void lock(final Entity... entities) {
            lock(Arrays.stream(entities).map(Object::toString).toArray(String[]::new));
        }

        static synchronized void unlock(final String... pathsAndNames) {
            for (final String pathAndName : pathsAndNames) {
                final ReentrantLock lock = lockedEntities.get(pathAndName);
                if (lock != null && lock.isHeldByCurrentThread()) {
                    if (DEBUG)
                        System.out.println("TRYING TO UNLOCK " + pathAndName);
                    lock.unlock();
                    if (DEBUG)
                        System.out.println("UNLOCKED " + pathAndName);
                } else if (DEBUG)
                    System.out.println("LOCK NOT FOUND/OWNED BY CALLER THREAD");
            }
        }

        static synchronized void unlock(final Entity... entities) {
            unlock(Arrays.stream(entities).map(Object::toString).toArray(String[]::new));
        }

        static synchronized boolean isLocked(final String... pathsAndNames) {
            for (final String pathAndName : pathsAndNames) {
                final ReentrantLock lock = lockedEntities.get(pathAndName);
                if (!(lock != null && lock.isLocked())) {
                    if (DEBUG)
                        System.out.println(pathAndName + " NOT LOCKED");
                    return false;
                }
            }
            if (DEBUG)
                System.out.println("ALL ENTITIES LOCKED");
            return true;
        }

        static synchronized boolean isLocked(final Entity... entities) {
            return isLocked(Arrays.stream(entities).map(Object::toString).toArray(String[]::new));
        }
    }

    @Override
    boolean equals(final Object obj);

    @Override
    int hashCode();

    @Override
    String toString();

    String getPath();

    String getName();

    void setPath(final String path);

    void setName(final String name);

    boolean doesExist();

    ErrorCode create(final String destination, final String... names);

    ErrorCode create(final String... names);

    private ErrorCode delete(final String destination, final String... names) { // for files and empty directories
        for (final String name : names)
            if ((getPath() + getName()).startsWith(destination + name))
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            else if (!Files.exists(Paths.get(destination, name)))
                return ErrorCode.ENTITY_NOT_FOUND;
        final String[] pathsAndNames = Arrays.stream(names).map(name -> destination + name).toArray(String[]::new);
        if (CriticalSectionHandler.isLocked(pathsAndNames))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(pathsAndNames);
        for (final String pathWithName : pathsAndNames) {
            if (DEBUG)
                System.out.println("DELETING " + pathWithName);
            try {
                Files.delete(Paths.get(pathWithName));
            } catch (final NoSuchFileException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.FILE_NOT_FOUND;
            } catch (final java.nio.file.DirectoryNotEmptyException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.DIR_NOT_EMPTY;
            } catch (final IOException e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.IO_ERROR;
            } catch (final Exception e) {
                CriticalSectionHandler.unlock(pathsAndNames);
                return ErrorCode.UNKOWN_ERROR;
            } finally {
                CriticalSectionHandler.unlock(pathWithName);
            }
        }
        return ErrorCode.SUCCESS;
    }

    default ErrorCode delete(final String... names) {
        return delete(getPath() + getName() + '/', names);
    }
}