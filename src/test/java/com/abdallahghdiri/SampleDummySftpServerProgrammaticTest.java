package com.abdallahghdiri;

import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * A sample JUNIT 5 declarative programmatic test.
 */
public class SampleDummySftpServerProgrammaticTest extends BaseSampleTest {


    private int PORT = 1234;
    private String USER = "ftp-user";
    private String PASSWORD = "ftp-password";

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
