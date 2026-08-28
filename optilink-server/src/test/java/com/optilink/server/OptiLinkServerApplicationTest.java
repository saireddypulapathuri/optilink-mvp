package com.optilink.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "optilink.transfer.root=${java.io.tmpdir}/optilink-context-test")
class OptiLinkServerApplicationTest {

    @Test
    void startsProductionApplicationContext() {
    }
}
