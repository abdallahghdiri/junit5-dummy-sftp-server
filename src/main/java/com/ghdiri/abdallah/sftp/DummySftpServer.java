package com.ghdiri.abdallah.sftp;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder.newLinux;
import static java.util.Collections.singletonList;

/**
 * A dummy SFTP server container.
 */
class DummySftpServer implements AutoCloseable {

    private final int port;
    private final Map<String, String> credentials;
    private final FileSystem fileSystem;
    private final SshServer server;

    private DummySftpServer(int port, Path hostKey, Map<String, String> credentials) {
        this.port = port;
        this.credentials = Collections.unmodifiableMap(credentials);

        // create the file system
        try {
            fileSystem = newLinux().build("InMemoryFileSystem");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an in-memory file system " +
                    "(check if another test failed to release resources)", e);
        }
        // start the sftp server
        try {
            server = start(fileSystem, hostKey);
        } catch (Exception e) {
            try {
                //attempt closing the file system so that it does not interfere with other tests
                fileSystem.close();
            } catch (Exception ce) {
                // do nothing
            }
            throw new RuntimeException("Failed to start the sftp server", e);
        }
    }

    static DummySftpServer create(int port, Path hostKey, Map<String, String> credentials) {
        return new DummySftpServer(port, hostKey, credentials);
    }

    private SshServer start(FileSystem fileSystem, Path hostKey) throws IOException {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        SimpleGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider(hostKey);
        sshServer.setKeyPairProvider(keyPairProvider);
        sshServer.setPasswordAuthenticator(this::authenticate);
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder().build();
        sshServer.setSubsystemFactories(singletonList(sftpSubsystemFactory));
        /* When a channel is closed SshServer calls close() on the file system.
         * In order to use the file system for multiple channels/sessions we
         * have to use a file system wrapper whose close() does nothing.
         */
        sshServer.setFileSystemFactory(session -> new DoNotClose(fileSystem));
        sshServer.start();

        return sshServer;
    }

    private boolean authenticate(
            String username,
            String password,
            ServerSession session
    ) {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && Objects.equals(
                credentials.get(username),
                password);
    }


    @Override
    public void close() throws Exception {
        fileSystem.close();
        if (server != null) { // if server fails to start
            server.stop();
        }
    }

    /**
     * @return the server port
     */
    public int getPort() {
        return server.getPort();
    }

    /**
     * @return the file system
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }


    private static class DoNotClose extends FileSystem {
        final FileSystem fileSystem;

        DoNotClose(
                FileSystem fileSystem
        ) {
            this.fileSystem = fileSystem;
        }

        @Override
        public FileSystemProvider provider() {
            return fileSystem.provider();
        }

        @Override
        public void close(
        ) throws IOException {
            //will not be closed
        }

        @Override
        public boolean isOpen() {
            return fileSystem.isOpen();
        }

        @Override
        public boolean isReadOnly() {
            return fileSystem.isReadOnly();
        }

        @Override
        public String getSeparator() {
            return fileSystem.getSeparator();
        }

        @Override
        public Iterable<Path> getRootDirectories() {
            return fileSystem.getRootDirectories();
        }

        @Override
        public Iterable<FileStore> getFileStores() {
            return fileSystem.getFileStores();
        }

        @Override
        public Set<String> supportedFileAttributeViews() {
            return fileSystem.supportedFileAttributeViews();
        }

        @Override
        public Path getPath(
                String first,
                String... more
        ) {
            return fileSystem.getPath(first, more);
        }

        @Override
        public PathMatcher getPathMatcher(
                String syntaxAndPattern
        ) {
            return fileSystem.getPathMatcher(syntaxAndPattern);
        }

        @Override
        public UserPrincipalLookupService getUserPrincipalLookupService() {
            return fileSystem.getUserPrincipalLookupService();
        }

        @Override
        public WatchService newWatchService() throws IOException {
            return fileSystem.newWatchService();
        }
    }
}
