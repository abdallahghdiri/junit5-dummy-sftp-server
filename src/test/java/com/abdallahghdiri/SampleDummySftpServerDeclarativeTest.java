package com.abdallahghdiri;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A sample JUNIT 5 declarative extension test
 */
@ExtendWith(DummySftpServerExtension.class)
public class SampleDummySftpServerDeclarativeTest {

    private String USER = "username";
    private String PASSWORD = "password";

    @Test
    public void testPayloadUpload(DummySftpServerExtension.SftpIoOperations ioOperations) throws Exception {

        String content = "sample content";
        String path = "text.txt";
        put(path, content);

        // check that file was uploaded
        Assertions.assertTrue(ioOperations.existsFile(path), "Failed to upload file to SFTP server");

        //get remote file contents
        String remoteFileContents = ioOperations.getFileContent(path);
        // check remote file contents
        assertThat("Wrong contents for remote file", remoteFileContents, equalTo(content));


    }

    private void put(String path, String contents) throws Exception {

        JSch jsch = new JSch();
        Session session = null;
        session = jsch.getSession(USER, "127.0.0.1", 23454);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(PASSWORD);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
        sftpChannel.put(new ByteArrayInputStream(bytes), path);
        sftpChannel.exit();
        session.disconnect();

    }

}
