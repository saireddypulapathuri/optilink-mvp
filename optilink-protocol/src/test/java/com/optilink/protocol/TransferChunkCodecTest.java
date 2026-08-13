package com.optilink.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferChunkCodecTest {

    @Test
    void chunksRoundTripAndReassemble() {
        UUID transferId = UUID.randomUUID();
        byte[] source = "chunk me across optical frames".getBytes(StandardCharsets.UTF_8);
        TransferChunker chunker = new TransferChunker();
        TransferChunkCodec codec = new TransferChunkCodec();
        TransferReassembler reassembler = new TransferReassembler();

        var chunks = chunker.chunk(transferId, source, 7);
        chunks.forEach(chunk -> reassembler.accept(codec.decode(codec.encode(chunk))));

        assertTrue(chunks.size() > 1);
        assertEquals(chunks.size(), reassembler.expectedChunks());
        assertEquals(chunks.size(), reassembler.acceptedChunks());
        assertEquals(transferId, reassembler.transferId().orElseThrow());
        assertArrayEquals(source, reassembler.assemble());
    }
}
