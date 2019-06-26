package com.ghdiri.abdallah.sftp;


import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.file.FileSystem;

import static com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder.newLinux;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests trying to provoke managed exception and check expected behaviour.
 */
public class ManagedExceptionsTest {

    /**
     * Max available port is 65535.
     */
    private static final int BAD_PORT = 70000;

    @Test
    void badPortTest() {
        // testing server start failure
        ExtensionContext extensionContext = mock(ExtensionContext.class);

        DummySftpServerExtension extension = DummySftpServerExtension.Builder.create()
                .port(BAD_PORT)
                .addCredentials("any", "any")
                .build();

        RuntimeException actual =
                Assertions.assertThrows(RuntimeException.class, () -> extension.beforeEach(extensionContext));
        MatcherAssert.assertThat(actual.getMessage(), equalTo("Failed to start the sftp server"));

    }


    @Test
    void badInMemoryFileSystemCreationFailure() throws IOException {

        // create the in memory file system outside the test scope
        try (FileSystem any = newLinux().build("InMemoryFileSystem")) { // auto-closable

            ExtensionContext extensionContext = mock(ExtensionContext.class);

            DummySftpServerExtension extension = new DummySftpServerExtension();

            RuntimeException actual =
                    Assertions.assertThrows(RuntimeException.class, () -> extension.beforeEach(extensionContext));
            MatcherAssert.assertThat(actual.getMessage(), equalTo("Failed to create an in-memory file system "
                    + "(check if another test failed to release resources)"));

        }

    }
}
