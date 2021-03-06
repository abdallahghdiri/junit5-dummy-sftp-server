package com.ghdiri.abdallah.sftp;

import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * A sample JUNIT 5 declarative programmatic test.
 */
public class SampleDummySftpServerProgrammaticTest extends AbstractSampleTest {
    private static final int PORT = 1234;
    private static final String USER = "ftp-user";
    private static final String PASSWORD = "ftp-password";

    @RegisterExtension
    static final DummySftpServerExtension extension = DummySftpServerExtension.Builder.create()
            .port(1234)
            .addCredentials("ftp-user", "ftp-password")
            .build();

    @Override
    protected int getPort(SftpGateway gateway) {
        return PORT;
    }

    @Override
    protected String getUserName() {
        return USER;
    }

    @Override
    protected String getPassword() {
        return PASSWORD;
    }
}
