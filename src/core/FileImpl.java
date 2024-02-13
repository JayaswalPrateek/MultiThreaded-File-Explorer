package core;

public final class FileImpl implements File {
    private String name, path;

    public FileImpl(final String name, final String path) {
        this.name = name;
        this.path = path;
    }

    public FileImpl(final String name, final String path, final FileImpl obj) {
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

    public ErrorCode open() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'open'");
    }

    public ErrorCode properties() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'properties'");
    }
}
