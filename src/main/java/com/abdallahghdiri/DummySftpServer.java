package com.abdallahghdiri;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder.newLinux;
import static java.nio.file.Files.*;
import static java.util.Collections.singletonList;

/**
 * A dummy SFTP server container.
 */
public class DummySftpServer implements AutoCloseable {

    private final int port;
    private final Map<String, String> credentials;
    private final FileSystem fileSystem;
    private final SshServer server;

    private DummySftpServer(int port, Map<String, String> credentials) {
        this.port = port;
        this.credentials = Collections.unmodifiableMap(credentials);

        try {
            fileSystem = newLinux().build("FakeSftpServerRule@" + hashCode());
            server = start(fileSystem);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to start the sftp server");
        }
    }

    public static DummySftpServer create(int port, Map<String, String> credentials) {
        return new DummySftpServer(port, credentials);
    }

    private SshServer start(FileSystem fileSystem) throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setPort(port);
        SimpleGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider();
        server.setKeyPairProvider(keyPairProvider);
        server.setPasswordAuthenticator(this::authenticate);
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder().build();
        server.setSubsystemFactories(singletonList(sftpSubsystemFactory));
        // get the file system that was crated
        server.setFileSystemFactory(session -> fileSystem);
        server.start();
        return server;
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
        server.stop();
        fileSystem.close();
    }

    /**
     * Checks the existence of a file. returns {@code true} iff the file exists
     * and it is not a directory.
     *
     * @param path the path to the file.
     * @return {@code true} iff the file exists and it is not a directory.
     * @throws IllegalStateException if not called from within a test.
     */
    public boolean existsFile(
            String path
    ) {
        verifyThatTestIsRunning("check existence of file");
        Path pathAsObject = fileSystem.getPath(path);
        return exists(pathAsObject) && !isDirectory(pathAsObject);
    }

    /**
     * Get a text file from the SFTP server. The file is decoded using the
     * specified encoding.
     *
     * @param path     the path to the file.
     * @param encoding the file's encoding.
     * @return the content of the text file.
     * @throws IOException           if the file cannot be read.
     * @throws IllegalStateException if not called from within a test.
     */
    public String getFileContent(
            String path,
            Charset encoding
    ) throws IOException {
        byte[] content = getFileContent(path);
        return new String(content, encoding);
    }

    /**
     * Get a file from the SFTP server.
     *
     * @param path the path to the file.
     * @return the content of the file.
     * @throws IOException           if the file cannot be read.
     * @throws IllegalStateException if not called from within a test.
     */
    public byte[] getFileContent(
            String path
    ) throws IOException {
        verifyThatTestIsRunning("download file");
        Path pathAsObject = fileSystem.getPath(path);
        return readAllBytes(pathAsObject);
    }

    private void verifyThatTestIsRunning(
            String mode
    ) {
        if (!fileSystem.isOpen())
            throw new IllegalStateException(
                    "Failed to " + mode + " because test has not been started or"
                            + " is already finished."
            );
    }
}
