package com.ghdiri.abdallah.sftp;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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

    private DummySftpServer(int port, Map<String, String> credentials) {
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
            server = start(fileSystem);
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

    static DummySftpServer create(int port, Map<String, String> credentials) {
        return new DummySftpServer(port, credentials);
    }

    private SshServer start(FileSystem fileSystem) throws IOException {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        SimpleGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider();
        sshServer.setKeyPairProvider(keyPairProvider);
        sshServer.setPasswordAuthenticator(this::authenticate);
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder().build();
        sshServer.setSubsystemFactories(singletonList(sftpSubsystemFactory));
        // get the file system that was crated
        sshServer.setFileSystemFactory(session -> fileSystem);
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
}
