package com.ghdiri.abdallah.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A test to validate the behaviour of passing a host-key and validating against it
 */
public class DummySftpHostKeyTest extends AbstractTest {
    private static final int PORT = 1234;
    private static final String USER = "ftp-user";
    private static final String PASSWORD = "ftp-password";

    private static final String HOST_KEY = "AAAAB3NzaC1yc2EAAAADAQABAAABAQCVgWZi4mZDa1l111ABmYcHYfGtcW4v59yWI/DG1hEgJeH1Y/Gk8dDauEvkLj5f1PCGpbSqlgGvcGteWItG0ikW4t3LveeYVyuqZ9jWOHeah2Q5wHVbv5rzQD+Gv+kDiUJ4rLJZb7BOEIL31LBKMtdhOtuolMcPeZqVEwr8YayBxPzJGu5EeFCoB7AC78aV7bJXx2eAnj829CF311Lc2TTsU4LYzx5GVc3f2nYCiE1owr9pSkF0ZH2c0XWgGt7Vk+I6OAlq6Ob5iPrfCA8np0Edfjhl0sf1C1Anm3puZ6CbzkytaOmvd4C+48wyd048yUZjX4w3EZXraf/XhwhA90hr";
    private static final String INVALID_HOST_KEY = "AAAAB3NzaC1yc2EAAAADAQABAAABAQCWu1jrUpfPEzEwDMSdCILhtWkS8lpjzDSrayiBhzyGoRUCCo0zLIv6oBDN3fe27qm5WBLv2OCPkG1HPjrfKqkds/s6DzOjTRp8oeiErx4h3+pfDTHkPCb6VnzL1D6rjJx5cfpHUifiwVf8czW9SEAXvUeZa2MtgylVTOLavgFe6wusmDD0UBDZpZ6HkR62hL/qFL7BnxxU7rdZ8p3NUIejAUvTk6bu0U7ilXbGR+ulBEOE1zL7Mb8N8di26yYNEHyTT1QB73vl6gPggPg6E/sJ6oOLm1JTyuad3+i0whXTQAAcb/hugw1jw6DLw7ywgFCGuMXMGA1O5KYENZtYvJ1h";

    @RegisterExtension
    static final DummySftpServerExtension extension = DummySftpServerExtension.Builder.create()
            .port(1234)
            .addCredentials("ftp-user", "ftp-password")
            .hostKey(Paths.get("src/test/resources/sftp_rsa"))
            .build();

    @Test
    public void testPayloadUploadWithHostKey(SftpGateway gateway) throws Exception {
        String content = "sample content";
        String path = "text.txt";

        Session session = createSessionWithHostKey(HOST_KEY);
        put(session, path, content);

        // check that file was uploaded
        Assertions.assertTrue(gateway.fileExists(path), "Failed to upload file to SFTP server");

        //get remote file contents
        String remoteFileContents = gateway.getFile(path);
        // check remote file contents
        assertThat("Wrong contents for remote file", remoteFileContents, equalTo(content));
    }

    @Test
    public void testConnectionFailedWithInvalidHostKey() {
        JSchException actual = assertThrows(JSchException.class,
                () -> createSessionWithHostKey(INVALID_HOST_KEY)
        );

        assertThat(actual.getMessage(), equalTo("HostKey has been changed: [localhost]:1234"));
    }

    private Session createSessionWithHostKey(String hostKey) throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(prepareKnownHostsStream(hostKey));

        Session session = jsch.getSession(USER, "localhost", PORT);
        session.setPassword(PASSWORD);
        session.connect();

        return session;
    }

    private InputStream prepareKnownHostsStream(String hostKey) {
        String knownHostLine = "[localhost]:" + PORT + " ssh-rsa " + hostKey + "\n";
        return new ByteArrayInputStream(knownHostLine.getBytes(StandardCharsets.UTF_8));
    }

}
