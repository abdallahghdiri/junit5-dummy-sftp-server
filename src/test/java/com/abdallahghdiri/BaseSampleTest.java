package com.abdallahghdiri;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A base sample test.
 */
public abstract class BaseSampleTest {

    @Test
    public void testPayloadUpload(SftpGateway gateway) throws Exception {

        String content = "sample content";
        String path = "text.txt";

        Session session = createSession(getPort(gateway), getUserName(), getPassword());
        put(session, path, content);

        // check that file was uploaded
        Assertions.assertTrue(gateway.fileExists(path), "Failed to upload file to SFTP server");

        //get remote file contents
        String remoteFileContents = gateway.getFile(path);
        // check remote file contents
        assertThat("Wrong contents for remote file", remoteFileContents, equalTo(content));
    }

    protected abstract int getPort(SftpGateway gateway);

    protected abstract String getUserName();

    protected abstract String getPassword();

    private Session createSession(int port, String username, String password) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, "localhost", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();

        return session;
    }

    private void put(Session session, String path, String contents) throws Exception {

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
        sftpChannel.put(new ByteArrayInputStream(bytes), path);
        sftpChannel.exit();
        session.disconnect();
    }


}
