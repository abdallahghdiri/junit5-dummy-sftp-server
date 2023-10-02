package com.ghdiri.abdallah.sftp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A dummy SFTP server extension.
 */
public class DummySftpServerExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

    private int port;
    private final Path hostKeyPath;
    private final Map<String, String> credentials;

    private DummySftpServer sftpServer;

    public DummySftpServerExtension() {
        hostKeyPath = null;
        credentials = new HashMap<>();
        credentials.put("username", "password");
    }

    public DummySftpServerExtension(int port, Path hostKeyPath, Map<String, String> credentials) {
        this.port = port;
        this.hostKeyPath = hostKeyPath;
        this.credentials = credentials;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        sftpServer = DummySftpServer.create(port, hostKeyPath, credentials);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        sftpServer.close();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == SftpGateway.class; // only support sftp io operation parameter
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return new SftpGatewayImpl(sftpServer.getPort(), sftpServer.getFileSystem());
    }


    public int getPort() {
        if (sftpServer == null) {
            return port;
        }
        return sftpServer.getPort();
    }

    /**
     * A {@link DummySftpServerExtension} builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private int port;
        private Path hostKeyPath;
        private final Map<String, String> credentials = new HashMap<>();

        /**
         * @return The builder
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Set the port.
         *
         * @param port The server port
         * @return The builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the HOST Key path.
         * Defining a HOST Key is not required as one will be generated automatically if not needed.
         *
         * @param hostKeyPath The path to HOST Key certificate
         * @return The builder
         */
        public Builder hostKey(Path hostKeyPath) {
            this.hostKeyPath = hostKeyPath;
            return this;
        }

        /**
         * Add the accepted credentials.
         *
         * @param userName The username
         * @param password The password
         * @return The builder
         */
        public Builder addCredentials(String userName, String password) {
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                credentials.put(userName, password);
            }
            return this;
        }

        /**
         * Build the extension.
         *
         * @return the extension
         * @throws IllegalArgumentException if port ios invalid
         */
        public DummySftpServerExtension build() {
            return new DummySftpServerExtension(port, hostKeyPath, credentials);
        }
    }

}
