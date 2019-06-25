package com.abdallahghdiri;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A dummy SFTP server extension.
 */
public class DummySftpServerExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

    private int port;
    private final Map<String, String> credentials;

    private DummySftpServer sftpServer;

    public DummySftpServerExtension() {
        credentials = new HashMap<>();
        credentials.put("username", "password");
    }


    public DummySftpServerExtension(int port, Map<String, String> credentials) {
        this.port = port;
        this.credentials = credentials;
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
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == SftpGateway.class; // only support sftp io operation parameter
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return new SftpGatewayImpl(sftpServer.getPort(), sftpServer.getFileSystem());
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private int port;
        private Map<String, String> credentials = new HashMap<>();

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
            // validate port
            if (port < 1 || port > 65535)
                throw new IllegalArgumentException(
                        "Port cannot be set to " + port
                                + " because only ports between 1 and 65535 are valid."
                );
            return new DummySftpServerExtension(port, credentials);
        }
    }

}
