package core;

final class FolderImpl implements Folder {
    private String name, path;

    FolderImpl(final String name, final String path) {
        this.name = name;
        this.path = path;
    }

    FolderImpl(final String name, final String path, final FolderImpl obj) {
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

    @Override
    public ErrorCode create(final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ErrorCode create(final String destination, final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ErrorCode copy(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public ErrorCode copy(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public ErrorCode delete(final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public ErrorCode delete(final String destination, final String... names) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public ErrorCode move(final String destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public ErrorCode move(final String destination, final String... newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public ErrorCode rename(final String newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rename'");
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    @Override
    public ErrorCode list() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'list'");
    }

    @Override
    public ErrorCode regexFilter(final String pattern) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'regexFilter'");
    }

    @Override
    public ErrorCode stepIn(final String target) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stepIn'");
    }

    @Override
    public ErrorCode stepOut() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stepOut'");
    }
}
