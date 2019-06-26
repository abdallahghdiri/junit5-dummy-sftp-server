package com.ghdiri.abdallah;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for different SFTP operation available through the {@link SftpGateway} accepted test method parameter.
 */
@ExtendWith(DummySftpServerExtension.class)
public class SftpOperationsTest extends AbstractTest {

    private static String SAMPLE_CONTENTS = "sample";


    ///**
    // * Programmatic declaration for credential definition flexibility.
    // */
    //@RegisterExtension
    //static final DummySftpServerExtension extension = DummySftpServerExtension.Builder.create()
    //        .port(1234)
    //        .addCredentials("admin", "all-powerful")
    //        .addCredentials("user", "let-me-in")
    //        .build();


    @Test
    void testInputStreamUpload(SftpGateway gateway) throws Exception {

        InputStream is = new ByteArrayInputStream(UTF_8.encode(SAMPLE_CONTENTS).array());
        String pathToFile = "/input/stream/stream-file.txt";
        testInDefaultContext(gateway, () -> {
            gateway.createDirectories("/input/stream");
            gateway.putFile(pathToFile, is);
        }, c -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            c.get(pathToFile, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            String stringContents = new String(bytes);
            assertThat("Bad contents", stringContents, equalTo(SAMPLE_CONTENTS));
        });
    }


}
