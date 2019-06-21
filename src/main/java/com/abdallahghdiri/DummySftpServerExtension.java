package com.abdallahghdiri;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DummySftpServerExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

    private final int port;
    private final Map<String, String> credentials;

    private DummySftpServer sftpServer;

    public DummySftpServerExtension() {
        port = 23454;
        credentials = new HashMap<>();
        credentials.put("username", "password");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        sftpServer = DummySftpServer.create(port, credentials);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        sftpServer.close();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == SftpIoOperations.class; // only support sftp io operation parameter
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return new SftpIoOperationsImpl();
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SftpServerExtensionBuilder {

        private int port;
        private Map<String, String> credentials = new HashMap<>();

        public static SftpServerExtensionBuilder create() {
            return new SftpServerExtensionBuilder();
        }

        /**
         * Set the port.
         *
         * @param port
         * @return
         */
        public SftpServerExtensionBuilder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Add the accepted credentials.
         *
         * @param userName The username
         * @param password The password
         * @return
         */
        public SftpServerExtensionBuilder addCredentials(String userName, String password) {
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                credentials.put(userName, password);
            }
            return this;
        }

        /**
         * Build the extension.
         *
         * @return
         */
        public DummySftpServerExtension build() {
            return new DummySftpServerExtension(port, credentials);
        }
    }

    /**
     * Sftp io operation
     */
    public interface SftpIoOperations {

        /**
         * Check if file exists for provided <code></code>
         *
         * @param path The file path
         * @return
         */
        boolean existsFile(String path);

        /**
         * Get file contents of provided <code>path</code> using the <code>charset</code>.
         *
         * @param path    The file path
         * @param charset The used charset
         * @return
         */
        String getFileContent(String path, Charset charset) throws IOException;

        /**
         * Get UTF-8 encoded file contents of provided file <code>path</code>.
         * @param path The file path
         * @return
         * @throws IOException
         */
        default String getFileContent(String path) throws IOException {
            return getFileContent(path, StandardCharsets.UTF_8);
        }
    }

    private class SftpIoOperationsImpl implements SftpIoOperations {
        @Override
        public boolean existsFile(String path) {
            return sftpServer.existsFile(path);
        }

        @Override
        public String getFileContent(String path, Charset charset) throws IOException {
            return sftpServer.getFileContent(path, charset);
        }
    }


}
