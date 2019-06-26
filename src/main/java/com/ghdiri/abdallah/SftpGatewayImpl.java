package com.ghdiri.abdallah;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A {@link SftpGateway} implementation.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SftpGatewayImpl implements SftpGateway {

    private final int port;
    private final FileSystem fs;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean fileExists(String path) {
        return Files.exists(fs.getPath(path));
    }

    @Override
    public void putFile(String path, String contents, Charset charset) throws IOException {
        Path filePath = fs.getPath(path);
        Files.write(filePath, contents.getBytes(charset));
    }

    @Override
    public void putFile(String path, InputStream inputStream) throws IOException {
        Path filePath = fs.getPath(path);
        Files.copy(inputStream, filePath);
    }

    @Override
    public void createDirectories(String... paths) throws IOException {
        for (String path : paths) {
            Files.createDirectories(fs.getPath(path));
        }
    }


    @Override
    public String getFile(String path, Charset charset) throws IOException {
        byte[] bytes = getFileBytes(path);
        return new String(bytes, charset);
    }

    @Override
    public byte[] getFileBytes(String path) throws IOException {
        Path filePath = fs.getPath(path);
        return Files.readAllBytes(filePath);
    }

    @Override
    public void recursiveDelete(String... paths) throws IOException {
        for (String path : paths) {
            try (Stream<Path> traverser = Files.walk(fs.getPath(path));) {
                traverser.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

        }
    }
}
