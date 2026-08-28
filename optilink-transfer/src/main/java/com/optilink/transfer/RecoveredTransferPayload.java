package com.optilink.transfer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Decrypted optical payload with portable file metadata.
 *
 * @param kind text, image, video, or file
 * @param name original display name
 * @param contentType media type
 * @param bytes recovered payload bytes
 */
public record RecoveredTransferPayload(String kind, String name, String contentType, byte[] bytes) {

    /**
     * Creates an immutable recovered payload.
     */
    public RecoveredTransferPayload {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(bytes, "bytes");
        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof RecoveredTransferPayload other
                && kind.equals(other.kind)
                && name.equals(other.name)
                && contentType.equals(other.contentType)
                && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, name, contentType, Arrays.hashCode(bytes));
    }
}
