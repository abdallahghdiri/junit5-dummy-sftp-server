package com.ghdiri.abdallah;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A sample JUNIT 5 declarative extension test.
 */
@ExtendWith(DummySftpServerExtension.class)
public class SampleDummySftpServerDeclarativeTest extends AbstractSampleTest {

    @Override
    protected int getPort(SftpGateway gateway) {
        return gateway.getPort();
    }

    @Override
    protected String getUserName() {
        return DEFAULT_USER;
    }

    @Override
    protected String getPassword() {
        return DEFAULT_PASSWORD;
    }
}
