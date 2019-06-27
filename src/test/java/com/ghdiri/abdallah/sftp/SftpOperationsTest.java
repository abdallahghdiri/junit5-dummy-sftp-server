package com.ghdiri.abdallah.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
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

    @Test
    void testStringFileUpload(SftpGateway gateway) throws Exception {

        String pathToFile = "/input/stream/stream-file.txt";
        testInDefaultContext(gateway, () -> {
            gateway.createDirectories("/input/stream");
            gateway.putFile(pathToFile, SAMPLE_CONTENTS);
        }, c -> checkFileContents(c, pathToFile, SAMPLE_CONTENTS));
    }

    @Test
    void testInputStreamUpload(SftpGateway gateway) throws Exception {

        InputStream is = new ByteArrayInputStream(UTF_8.encode(SAMPLE_CONTENTS).array());
        String pathToFile = "/string/string-file.txt";
        testInDefaultContext(gateway, () -> {
            gateway.createDirectories("/string");
            gateway.putFile(pathToFile, is);
        }, c -> checkFileContents(c, pathToFile, SAMPLE_CONTENTS));
    }

    @Test
    void testRecursiveDirectoryDelete(SftpGateway gateway) throws Exception {

        String path= "/root/sub-dir/string-file.txt";
        String secondPath= "/root/stream-file.txt";
        testInDefaultContext(gateway, () -> {
            gateway.createDirectories("/root/sub-dir");
            gateway.putFile(path, SAMPLE_CONTENTS);
            gateway.putFile(secondPath, SAMPLE_CONTENTS);
        }, c -> {
            // check that the 2 files were uploaded
            checkFileContents(c, path, SAMPLE_CONTENTS);
            checkFileContents(c, secondPath, SAMPLE_CONTENTS);
            gateway.recursiveDelete("/root/sub-dir");
        });
    }

    private void checkFileContents(ChannelSftp channel, String pathToFile, String expectedContents) throws SftpException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        channel.get(pathToFile, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        String stringContents = new String(bytes);
        assertThat("Bad contents", stringContents, equalTo(expectedContents));
    }



}
