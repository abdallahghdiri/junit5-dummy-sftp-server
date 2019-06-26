package com.ghdiri.abdallah.sftp;

import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A base sample test. A {@link DummySftpServerExtension} showcase.
 */
public abstract class AbstractSampleTest extends AbstractTest {

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

}
