package com.optilink.session;

import com.optilink.core.DeviceId;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Canonical transcript material bound into signatures and HKDF salt.
 *
 * @param localDevice local device identity
 * @param remoteDevice remote device identity
 * @param localEphemeralPublicKey local X25519 public key
 * @param remoteEphemeralPublicKey remote X25519 public key
 */
public record HandshakeTranscript(
        DeviceId localDevice,
        DeviceId remoteDevice,
        PublicKey localEphemeralPublicKey,
        PublicKey remoteEphemeralPublicKey) {

    /**
     * Creates a handshake transcript.
     */
    public HandshakeTranscript {
        Objects.requireNonNull(localDevice, "localDevice");
        Objects.requireNonNull(remoteDevice, "remoteDevice");
        Objects.requireNonNull(localEphemeralPublicKey, "localEphemeralPublicKey");
        Objects.requireNonNull(remoteEphemeralPublicKey, "remoteEphemeralPublicKey");
    }

    /**
     * Returns a SHA-256 transcript hash.
     *
     * @return transcript hash
     */
    public byte[] sha256() {
        try {
            return MessageDigest.getInstance("SHA-256").digest(encode());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private byte[] encode() {
        byte[] localKey = localEphemeralPublicKey.getEncoded();
        byte[] remoteKey = remoteEphemeralPublicKey.getEncoded();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 4 + Integer.BYTES * 2 + localKey.length + remoteKey.length);
        buffer.putLong(localDevice.value().getMostSignificantBits());
        buffer.putLong(localDevice.value().getLeastSignificantBits());
        buffer.putLong(remoteDevice.value().getMostSignificantBits());
        buffer.putLong(remoteDevice.value().getLeastSignificantBits());
        buffer.putInt(localKey.length);
        buffer.put(localKey);
        buffer.putInt(remoteKey.length);
        buffer.put(remoteKey);
        return buffer.array();
    }
}
