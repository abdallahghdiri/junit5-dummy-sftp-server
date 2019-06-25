package com.abdallahghdiri;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A sample JUNIT 5 declarative extension test.
 */
@ExtendWith(DummySftpServerExtension.class)
public class SampleDummySftpServerDeclarativeTest extends BaseSampleTest {

    private String USER = "username";
    private String PASSWORD = "password";

    @Override
    protected int getPort(SftpGateway gateway) {
        return gateway.getPort();
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
