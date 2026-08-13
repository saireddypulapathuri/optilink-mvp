package com.optilink.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class SessionKeyDeriverTest {

    @Test
    void rolesReceiveOppositeDirectionalKeys() throws Exception {
        SessionKeyDeriver deriver = new SessionKeyDeriver(new HkdfSha256());
        byte[] secret = new byte[32];
        byte[] transcript = new byte[32];
        secret[0] = 7;
        transcript[0] = 9;

        SessionKeys initiator = deriver.derive(secret, transcript, PeerRole.INITIATOR);
        SessionKeys responder = deriver.derive(secret, transcript, PeerRole.RESPONDER);

        assertArrayEquals(initiator.outboundKey(), responder.inboundKey());
        assertArrayEquals(initiator.inboundKey(), responder.outboundKey());
    }
}
