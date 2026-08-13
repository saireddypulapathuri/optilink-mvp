package com.optilink.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Derives directional session traffic keys from an X25519 shared secret.
 */
public final class SessionKeyDeriver {
    private static final byte[] INFO = "OptiLink-v1-session-traffic".getBytes(StandardCharsets.US_ASCII);
    private static final int KEY_MATERIAL_BYTES = AesGcmPacketCipher.KEY_BYTES * 2;
    private final HkdfSha256 hkdf;

    /**
     * Creates a session key deriver.
     *
     * @param hkdf HKDF implementation
     */
    public SessionKeyDeriver(HkdfSha256 hkdf) {
        this.hkdf = Objects.requireNonNull(hkdf, "hkdf");
    }

    /**
     * Derives send and receive keys for a peer role.
     *
     * @param sharedSecret X25519 shared secret
     * @param transcriptHash authenticated transcript hash or salt
     * @param role local peer role
     * @return directional session keys
     * @throws GeneralSecurityException when HKDF fails
     */
    public SessionKeys derive(byte[] sharedSecret, byte[] transcriptHash, PeerRole role)
            throws GeneralSecurityException {
        Objects.requireNonNull(role, "role");
        byte[] material = hkdf.derive(transcriptHash.clone(), sharedSecret.clone(), INFO, KEY_MATERIAL_BYTES);
        byte[] first = Arrays.copyOfRange(material, 0, AesGcmPacketCipher.KEY_BYTES);
        byte[] second = Arrays.copyOfRange(material, AesGcmPacketCipher.KEY_BYTES, material.length);
        return role == PeerRole.INITIATOR ? new SessionKeys(first, second) : new SessionKeys(second, first);
    }
}
