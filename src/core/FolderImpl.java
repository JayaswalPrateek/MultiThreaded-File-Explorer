package core;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public final class FolderImpl implements Folder {
    private volatile String path, name;

    private static final class Parser {
        static String getPath(final String pathWithName) {
            return pathWithName.substring(0, 1 + pathWithName.lastIndexOf('/'));
        }

        static String getName(final String pathWithName) {
            return pathWithName.substring(1 + pathWithName.lastIndexOf('/'));
        }
    }

    private static final String homeDir = System.getProperty("user.home");
    private static final FolderImpl singletonObj = new FolderImpl(Parser.getPath(homeDir), Parser.getName(homeDir));
    private final ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static FolderImpl getInstance() {
        return singletonObj;
    }

    private FolderImpl(final String path, final String name) {
        this.path = path.endsWith("/") ? path : (path + '/');
        this.name = name;
        if (!doesExist())
            create(".", new String[] { name });
    }

    private FolderImpl(final String pathWithName) {
        this(pathWithName.substring(0, pathWithName.lastIndexOf('/' + 1)),
                pathWithName.substring(pathWithName.lastIndexOf('/' + 1)));
    }

    private FolderImpl(final String newName, final FolderImpl obj) {
        this(obj.getPath() + obj.getName(), newName);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean doesExist() {
        if (DEBUG)
            System.out.println("CHECKING IF " + path + name + " EXISTS: ");
        CriticalSectionHandler.lock(this);
        final boolean result = Files.exists(Path.of(path, name)) && Files.isDirectory(Path.of(path, name));
        CriticalSectionHandler.unlock(this);
        if (DEBUG)
            System.out.println((result ? "" : "NOT ") + "FOUND");
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
                .map(name -> this.getPath() + this.getName() + "/" + (destination.equals(".") ? ""
                        : destination) + name)
                .toArray(String[]::new);
        if (CriticalSectionHandler.isLocked(pathsAndNames))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(pathsAndNames);
        for (final String newFolderName : pathsAndNames) {
            if (DEBUG)
                System.out.println("CREATING " + newFolderName);
            try {
                Files.createDirectories(Paths.get(newFolderName));
            } catch (final UnsupportedOperationException e) {
                return ErrorCode.OPERATION_NOT_SUPPORTED;
            } catch (final java.nio.file.FileAlreadyExistsException e) {
                return ErrorCode.FILE_ALREADY_EXISTS;
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

    public ErrorCode createNewFile(final String destination, final String... newFileNames) {
        return new FileImpl().create(destination, newFileNames);
    }

    public ErrorCode createNewFile(final String... newFileNames) {
        return createNewFile(".", newFileNames);
    }

    public ErrorCode nonAsyncCopy(final String srcPath, final String srcName, final String destPath,
            final String destName) {
        final String srcFileLocation = this.getPath() + this.getName() + "/"
                + (srcPath.equals(".") ? "" : srcPath) + (srcPath.endsWith("/") ? "" : "/")
                + srcName;
        final String destFileLocation = this.getPath() + this.getName() + "/"
                + (destPath.equals(".") ? "" : destPath) + (destPath.endsWith("/") ? "" : "/")
                + destName;
        if (!Files.exists(Paths.get(srcFileLocation)))
            return ErrorCode.ENTITY_NOT_FOUND;
        if (CriticalSectionHandler.isLocked(srcFileLocation, destFileLocation))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(srcFileLocation, destFileLocation);
        if (DEBUG)
            System.out.println("COPYING " + srcFileLocation + " TO " + destFileLocation);
        try {
            if (Files.isRegularFile(Paths.get(srcFileLocation))) {
                Files.copy(Paths.get(srcFileLocation), Paths.get(destFileLocation),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                final Path sourcePath = Paths.get(srcFileLocation);
                final Path targetPath = Paths.get(destFileLocation);
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                            throws IOException {
                        final Path targetDirPath = targetPath.resolve(sourcePath.relativize(dir));
                        if (!Files.exists(targetDirPath))
                            Files.createDirectories(targetDirPath);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                            throws IOException {
                        Files.copy(file, targetPath.resolve(sourcePath.relativize(file)),
                                StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (final UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (final java.nio.file.FileAlreadyExistsException e) {
            return ErrorCode.FILE_ALREADY_EXISTS;
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        } finally {
            CriticalSectionHandler.unlock(srcFileLocation, destFileLocation);
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode nonAsyncCopy(final String destination, final String... names) {
        for (final String name : names) {
            ErrorCode err = nonAsyncCopy(".", name, destination, name);
            if (err != ErrorCode.SUCCESS)
                return err;
        }
        return ErrorCode.SUCCESS;
    }

    public Future<ErrorCode> copy(final String srcPath, final String srcName, final String destPath,
            final String destName) {
        final Callable<ErrorCode> copyTask = () -> nonAsyncCopy(srcPath, srcName, destPath, destName);
        Future<ErrorCode> result = executorService.submit(copyTask);
        return result;
    }

    public Future<ErrorCode> copy(final String destination, final String... names) {
        final List<Future<ErrorCode>> futures = new ArrayList<>();
        for (final String name : names) {
            final Future<ErrorCode> future = executorService.submit(() -> nonAsyncCopy(".", name, destination, name));
            futures.add(future);
        }

        return executorService.submit(() -> {
            for (final Future<ErrorCode> future : futures)
                try {
                    ErrorCode errorCode = future.get();
                    if (errorCode != ErrorCode.SUCCESS)
                        return errorCode;
                } catch (InterruptedException | ExecutionException e) {
                    return ErrorCode.UNKOWN_ERROR;
                }
            return ErrorCode.SUCCESS;
        });
    }

    private ErrorCode nonAsyncMove(final String srcPath, final String srcName, final String destPath,
            final String destName) {
        final String srcFileLocation = this.getPath() + this.getName() + "/" + (srcPath.equals(".") ? "" : srcPath)
                + (srcPath.endsWith("/") ? "" : "/")
                + srcName;
        final String destFileLocation = this.getPath() + this.getName() + "/" + (destPath.equals(".") ? "" : destPath)
                + (destPath.endsWith("/") ? "" : "/")
                + destName;
        if (!Files.exists(Paths.get(srcFileLocation)))
            return ErrorCode.ENTITY_NOT_FOUND;
        if (CriticalSectionHandler.isLocked(srcFileLocation, destFileLocation))
            return ErrorCode.ENTITY_IS_LOCKED;
        CriticalSectionHandler.lock(srcFileLocation, destFileLocation);
        if (DEBUG)
            System.out.println("MOVING " + srcFileLocation + " TO " + destFileLocation);
        try {
            Files.move(Path.of(srcFileLocation), Path.of(destFileLocation), StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedOperationException e) {
            return ErrorCode.OPERATION_NOT_SUPPORTED;
        } catch (IOException e) {
            return ErrorCode.IO_ERROR;
        } catch (Exception e) {
            return ErrorCode.UNKOWN_ERROR;
        } finally {
            CriticalSectionHandler.unlock(srcFileLocation, destFileLocation);
        }
        return ErrorCode.SUCCESS;
    }

    public Future<ErrorCode> move(final String srcPath, final String srcName, final String destPath,
            final String destName) {
        final Callable<ErrorCode> moveTask = () -> {
            return nonAsyncMove(srcPath, srcName, destPath, destName);
        };
        Future<ErrorCode> result = executorService.submit(moveTask);
        return result;
    }

    public Future<ErrorCode> move(final String destination, final String... names) {
        final List<Future<ErrorCode>> futures = new ArrayList<>();

        for (final String name : names) {
            final Future<ErrorCode> future = executorService.submit(() -> {
                return nonAsyncMove(".", name, destination, name);
            });
            futures.add(future);
        }

        return executorService.submit(() -> {
            for (final Future<ErrorCode> future : futures) {
                try {
                    final ErrorCode errorCode = future.get();
                    if (errorCode != ErrorCode.SUCCESS)
                        return errorCode;
                } catch (InterruptedException | ExecutionException e) {
                    return ErrorCode.UNKOWN_ERROR;
                }
            }
            return ErrorCode.SUCCESS;
        });
    }

    public Future<ErrorCode> rename(final String oldName, final String newName) {
        return move(".", oldName, ".", newName);
    }

    public CopyOnWriteArrayList<String> listFiles(final ListOption opt) {
        final CopyOnWriteArrayList<String> files = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name))) {
            for (final Path path : stream)
                if (!Files.isDirectory(path))
                    if (opt == ListOption.SHOW_HIDDEN || !Files.isHidden(path))
                        files.add(Parser.getName(path.toString()));
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        }
        files.sort(Comparator.naturalOrder());
        return files;
    }

    public CopyOnWriteArrayList<String> listFiles() {
        return listFiles(ListOption.NONE);
    }

    public CopyOnWriteArrayList<String> listFolders(final ListOption opt) {
        final CopyOnWriteArrayList<String> folders = new CopyOnWriteArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name),
                Files::isDirectory)) {
            for (final Path path : stream)
                if (opt == ListOption.SHOW_HIDDEN || !Files.isHidden(path))
                    folders.add(Parser.getName(path.toString()));
        } catch (final IOException | DirectoryIteratorException e) {
            if (DEBUG)
                e.printStackTrace();
        } catch (final Exception e) {
            System.out.println(ErrorCode.UNKOWN_ERROR);
        }
        folders.sort(Comparator.naturalOrder());
        return folders;
    }

    public CopyOnWriteArrayList<String> listFolders() {
        return listFolders(ListOption.NONE);
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString, final ListOption opt) {
        final CopyOnWriteArrayList<String> Filtered = new CopyOnWriteArrayList<String>();
        final Pattern pattern = Pattern.compile(patternString);
        for (final String candidateFile : listFiles(opt))
            if (pattern.matcher(candidateFile).matches())
                Filtered.add(candidateFile);
        for (final String candidateFolder : listFolders(opt))
            if (pattern.matcher(candidateFolder).matches())
                Filtered.add(candidateFolder);
        return Filtered;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString) {
        return regexFilter(patternString, ListOption.NONE);
    }

    public ErrorCode stepIn(final String target) {
        if (DEBUG)
            System.out.println("STEPPING IN FROM PATH=" + path + " NAME=" + name + " TO " + target);
        if (target.equals("."))
            return ErrorCode.SUCCESS;
        else if (target.equals(".."))
            stepOut();
        if (!listFolders().contains(target))
            return ErrorCode.DIR_NOT_FOUND;
        if (!(getPath() + getName()).equals("/"))
            setPath(getPath() + getName() + '/');
        else
            setPath("/");
        setName(target);
        return ErrorCode.SUCCESS;
    }

    public ErrorCode stepOut() {
        if (DEBUG)
            System.out.print("STEPPING OUT OF PATH=" + path + " NAME=" + name);
        if (path.equals("/"))
            return ErrorCode.DIR_NOT_FOUND;
        final int lastSlash = path.lastIndexOf('/');
        final int secondLastIndex = path.lastIndexOf('/', lastSlash - 1);
        setName(path.substring(1 + secondLastIndex, lastSlash));
        setPath(path.substring(0, secondLastIndex + 1));
        if (DEBUG)
            System.out.println(" TO PATH=" + path + " NAME=" + name);
        return ErrorCode.SUCCESS;
    }

    private ErrorCode handleAbsolutePath(String destination) {
        final String savedPath = path, savedName = name;
        if (destination.endsWith("/"))
            destination = destination.substring(0, destination.length() - 1);
        if (!Files.isDirectory(Paths.get(destination)))
            return ErrorCode.DIR_NOT_FOUND;
        path = "";
        name = "/";
        boolean skipFirst = true;
        for (final String segment : destination.split("/")) {
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            if (segment.equals("."))
                continue;
            else if (segment.equals("..")) {
                if (stepOut() == ErrorCode.DIR_NOT_FOUND) {
                    path = savedPath;
                    name = savedName;
                    return ErrorCode.DIR_NOT_FOUND;
                }
            } else {
                if (stepIn(segment) == ErrorCode.DIR_NOT_FOUND) {
                    path = savedPath;
                    name = savedName;
                    return ErrorCode.DIR_NOT_FOUND;
                }
            }
        }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode cd(final String destination) {
        if (DEBUG)
            System.out.println("CHANGE DIR TO " + destination);
        if (destination.equals("/")) {
            path = "";
            name = "/";
            return ErrorCode.SUCCESS;
        }
        if (destination.startsWith("/"))
            return handleAbsolutePath(destination);
        if (destination.startsWith("~"))
            return handleAbsolutePath(System.getProperty("user.home") +
                    destination.substring(1));
        for (final String segment : destination.split("/"))
            if (segment.equals("."))
                continue;
            else if (segment.equals("..")) {
                if (stepOut() == ErrorCode.DIR_NOT_FOUND)
                    return ErrorCode.DIR_NOT_FOUND;
            } else {
                if (stepIn(segment) == ErrorCode.DIR_NOT_FOUND)
                    return ErrorCode.DIR_NOT_FOUND;
            }
        return ErrorCode.SUCCESS;
    }

    public ErrorCode cd() {
        return cd(System.getProperty("user.home"));
    }

    public void shutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor service did not terminate");
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}