package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dipanshushukla.realtimechatappmessageservice.service.impl.ULIDServiceImpl;

class ULIDServiceImplTest {
    private ULIDServiceImpl ulidService;

    @BeforeEach
    void setUp() {
        ulidService = new ULIDServiceImpl();
    }

    @Test
    @DisplayName("Should generate a valid 16-byte array ULID")
    void newId_ShouldReturn16ByteArray() {
        // Act
        byte[] result = ulidService.newId();

        // Assert
        assertNotNull(result);
        assertEquals(16, result.length);
    }

    @Test
    @DisplayName("Should generate a valid 26-character String ULID")
    void newIdString_ShouldReturn26CharString() {
        // Act
        String result = ulidService.newIdString();

        // Assert
        assertNotNull(result);
        assertEquals(26, result.length());
        assertTrue(result.matches("^[0-9A-Z]{26}$"), "String should only contain valid ULID characters");
    }

}
