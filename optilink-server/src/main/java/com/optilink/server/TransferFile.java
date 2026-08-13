package com.optilink.server;

import java.nio.file.Path;
import java.util.Objects;
import org.springframework.http.MediaType;

/**
 * Safely resolved transfer file.
 *
 * @param path file path
 * @param mediaType response media type
 */
public record TransferFile(Path path, MediaType mediaType) {

    /**
     * Creates a transfer file view.
     */
    public TransferFile {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(mediaType, "mediaType");
    }
}
