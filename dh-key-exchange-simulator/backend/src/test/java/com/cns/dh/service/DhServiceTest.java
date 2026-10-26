package com.cns.dh.service;

import com.cns.dh.dto.DhRequest;
import com.cns.dh.dto.MitmRequest;
import com.cns.dh.repository.SimulationRecordRepository;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DhServiceTest {
    private final SimulationRecordRepository repository = mock(SimulationRecordRepository.class);
    private final DhService service = new DhService(repository);

    @Test
    void textbookExampleProducesSameSecret() {
        Map<String, Object> result = service.simulate(new DhRequest(
                BigInteger.valueOf(23), BigInteger.valueOf(5),
                BigInteger.valueOf(6), BigInteger.valueOf(15)));
        assertEquals("8", result.get("alicePublic"));
        assertEquals("19", result.get("bobPublic"));
        assertEquals("2", result.get("aliceSharedSecret"));
        assertEquals("2", result.get("bobSharedSecret"));
        assertEquals(true, result.get("secretsMatch"));
    }

    @Test
    void invalidPrimeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.simulate(new DhRequest(
                BigInteger.valueOf(21), BigInteger.valueOf(5),
                BigInteger.valueOf(6), BigInteger.valueOf(15))));
    }

    @Test
    void invalidExponentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.simulate(new DhRequest(
                BigInteger.valueOf(23), BigInteger.valueOf(5),
                BigInteger.ONE, BigInteger.valueOf(15))));
    }

    @Test
    void mitmCreatesTwoLegSecrets() {
        Map<String, Object> result = service.simulateMitm(new MitmRequest(
                BigInteger.valueOf(23), BigInteger.valueOf(5),
                BigInteger.valueOf(6), BigInteger.valueOf(15),
                BigInteger.valueOf(7), BigInteger.valueOf(11)));
        assertEquals(true, result.get("attackerDerivesBothLegSecrets"));
    }
}
