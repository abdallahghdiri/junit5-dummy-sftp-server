package com.ghdiri.abdallah;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * An SFTP server operations contract.
 */
public interface SftpGateway {

    /**
     * Get the server port.
     *
     * @return The server port
     */
    int getPort();


    /**
     * Check if a file exists.
     *
     * @return <code>true</code> if the file is found
     */
    boolean fileExists(String path);

    /**
     * Upload the <code>charset</code> encoded file {@link String} <code>contents</code> to the provided <code>path</code>.
     *
     * @param path     The destination file path
     * @param contents The file contents
     * @param charset  The encoding charset
     * @throws IOException if the file upload fails
     */
    void putFile(String path, String contents, Charset charset) throws IOException;

    /**
     * Upload the {@link StandardCharsets#UTF_8} file {@link String} <code>contents</code> to the provided <code>path</code>.
     *
     * @param path     The destination file path
     * @param contents The file contents
     * @throws IOException if the file upload fails
     */
    default void putFile(String path, String contents) throws IOException {
        putFile(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Upload the file using a <code>inputStream</code> to the provided <code>path</code>.
     *
     * @param path        The destination file path
     * @param inputStream The file input stream
     * @throws IOException if the file upload fails
     */
    void putFile(String path, InputStream inputStream) throws IOException;

    /**
     * Create directories using the provided <code>paths</code>
     *
     * @param paths The paths to be created
     * @throws IOException if the directory creation fails
     */
    void createDirectories(String... paths) throws IOException;

    /**
     * Get the file {@link String} value using the provide <code>path</code> encoded using <code>charset</code>.
     *
     * @param path    The source file path
     * @param charset The encoding charset
     * @return The file contents
     * @throws IOException if the file read fails
     */
    String getFile(String path, Charset charset) throws IOException;

    /**
     * Get the {@link StandardCharsets#UTF_8} encoded file {@link String} value using the provided <code>path</code> .
     *
     * @param path The source file path
     * @return The file contents
     * @throws IOException if the file read fails
     */
    default String getFile(String path) throws IOException {
        return getFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Get the file byte array contents using the provided <code>path</code>.
     *
     * @param path The source file path
     * @return The file byte array contents
     * @throws IOException if the file read fails
     */
    byte[] getFileBytes(String path) throws IOException;


    /**
     * Recursively delete all the provided <code>paths</code>.
     *
     * @param paths The paths to recursively delete
     * @throws IOException If directory delete fails
     */
    void recursiveDelete(String... paths) throws IOException;

}
