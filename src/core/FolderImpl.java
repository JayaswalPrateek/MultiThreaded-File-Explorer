package core;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FolderImpl implements Folder {
    private String name, path;

    public FolderImpl(final String name, final String path) {
        this.name = name;
        this.path = path + '/';
    }

    public FolderImpl(final String name, final String path, final FolderImpl obj) {
        this(name, path);
        // copy contents in obj to this
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private void setName(final String name) {
        this.name = name;
    }

    private void setPath(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return getPath() + getName();
    }

    public ErrorCode create(final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    public ErrorCode create(final String destination, final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    public ErrorCode copy(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    public ErrorCode copy(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    public ErrorCode delete(final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public ErrorCode delete(final String destination, final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public ErrorCode move(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    public ErrorCode move(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    public ErrorCode rename(final String newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rename'");
    }

    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public CopyOnWriteArrayList<String> listFolders() {
        CopyOnWriteArrayList<String> folders = new CopyOnWriteArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name),
                Files::isDirectory)) {
            for (Path path : stream) {
                folders.add(path.toString());
            }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }
        return folders;
    }

    private CopyOnWriteArrayList<String> getNameFromPathAndName(CopyOnWriteArrayList<String> entityList) {
        for (int i = 0; i < entityList.size(); i++) {
            String fullPath = entityList.get(i);
            String nameOnly = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (nameOnly.startsWith("."))
                nameOnly = nameOnly.substring(1 + nameOnly.indexOf('.'));
            entityList.set(i, nameOnly);
        }
        return entityList;
    }

    public CopyOnWriteArrayList<String> listFiles() {
        CopyOnWriteArrayList<String> files = new CopyOnWriteArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path + name))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    files.add(path.toString());
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }
        return files;
    }

    public CopyOnWriteArrayList<String> regexFilter(final String patternString) {
        final CopyOnWriteArrayList<String> Files = getNameFromPathAndName(listFiles());
        final CopyOnWriteArrayList<String> Folders = getNameFromPathAndName(listFolders());

        CopyOnWriteArrayList<String> Filtered = new CopyOnWriteArrayList<String>();
        final Pattern pattern = Pattern.compile(patternString);
        for (final String candidateFile : Files) {
            final Matcher matcher = pattern.matcher(candidateFile);
            final boolean matchFound = matcher.matches();
            if (matchFound)
                Filtered.add(candidateFile);
        }
        for (final String candidateFolder : Folders) {
            final Matcher matcher = pattern.matcher(candidateFolder);
            final boolean matchFound = matcher.matches();
            if (matchFound)
                Filtered.add(candidateFolder);
        }
        return Filtered;
    }

    public ErrorCode stepIn(final String target) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stepIn'");
    }

    public ErrorCode stepOut() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stepOut'");
    }
}
