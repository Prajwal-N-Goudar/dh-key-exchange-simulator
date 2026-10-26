
package com.cns.dh.service;

import com.cns.dh.dto.DhRequest;
import com.cns.dh.dto.MitmRequest;
import com.cns.dh.dto.HistoryResponse;
import com.cns.dh.model.SimulationRecord;
import com.cns.dh.repository.SimulationRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
public class DhService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BigInteger TWO = BigInteger.TWO;
    private static final BigInteger MAX_P =
            BigInteger.valueOf(Integer.MAX_VALUE);

    private final SimulationRecordRepository repository;

    public DhService(SimulationRecordRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> simulate(DhRequest request) {
        Parameters params = validate(request.p(), request.g());

        BigInteger a = privateExponent(
                request.alicePrivate(), params.p(), "alicePrivate");

        BigInteger b = privateExponent(
                request.bobPrivate(), params.p(), "bobPrivate");

        BigInteger alicePublic = params.g().modPow(a, params.p());
        BigInteger bobPublic = params.g().modPow(b, params.p());

        BigInteger aliceSecret = bobPublic.modPow(a, params.p());
        BigInteger bobSecret = alicePublic.modPow(b, params.p());

        boolean match = aliceSecret.equals(bobSecret);

        repository.save(new SimulationRecord(
                "NORMAL",
                params.p().toString(),
                params.g().toString(),
                alicePublic.toString(),
                bobPublic.toString(),
                match
        ));

        return Map.ofEntries(
                Map.entry("mode", "NORMAL"),
                Map.entry("p", params.p().toString()),
                Map.entry("g", params.g().toString()),
                Map.entry("alicePrivate", a.toString()),
                Map.entry("bobPrivate", b.toString()),
                Map.entry("alicePublic", alicePublic.toString()),
                Map.entry("bobPublic", bobPublic.toString()),
                Map.entry("aliceSharedSecret", aliceSecret.toString()),
                Map.entry("bobSharedSecret", bobSecret.toString()),
                Map.entry("secretsMatch", match),
                Map.entry("steps", List.of(
                        "Alice chooses private exponent a.",
                        "Bob chooses private exponent b.",
                        "Alice publishes A = g^a mod p.",
                        "Bob publishes B = g^b mod p.",
                        "Alice computes B^a mod p.",
                        "Bob computes A^b mod p."
                )),
                Map.entry(
                        "warning",
                        "Educational small-number simulation only; do not use these values to protect real data."
                )
        );
    }

    public Map<String, Object> simulateMitm(MitmRequest request) {
        Parameters params = validate(request.p(), request.g());

        BigInteger a = privateExponent(
                request.alicePrivate(), params.p(), "alicePrivate");

        BigInteger b = privateExponent(
                request.bobPrivate(), params.p(), "bobPrivate");

        BigInteger x = privateExponent(
                request.attackerPrivateForAlice(),
                params.p(),
                "attackerPrivateForAlice"
        );

        BigInteger y = privateExponent(
                request.attackerPrivateForBob(),
                params.p(),
                "attackerPrivateForBob"
        );

        BigInteger alicePublic = params.g().modPow(a, params.p());
        BigInteger bobPublic = params.g().modPow(b, params.p());

        // The attacker establishes separate shared secrets with Alice and Bob.
        BigInteger attackerPublicToAlice =
                params.g().modPow(x, params.p());

        BigInteger attackerPublicToBob =
                params.g().modPow(y, params.p());

        BigInteger aliceSecret =
                attackerPublicToAlice.modPow(a, params.p());

        BigInteger attackerSecretWithAlice =
                alicePublic.modPow(x, params.p());

        BigInteger bobSecret =
                attackerPublicToBob.modPow(b, params.p());

        BigInteger attackerSecretWithBob =
                bobPublic.modPow(y, params.p());

        boolean attackerCanReadBothLegs =
                aliceSecret.equals(attackerSecretWithAlice)
                && bobSecret.equals(attackerSecretWithBob);

        repository.save(new SimulationRecord(
                "MITM",
                params.p().toString(),
                params.g().toString(),
                alicePublic.toString(),
                bobPublic.toString(),
                aliceSecret.equals(bobSecret)
        ));

        return Map.ofEntries(
                Map.entry("mode", "MITM"),
                Map.entry("p", params.p().toString()),
                Map.entry("g", params.g().toString()),
                Map.entry("alicePublic", alicePublic.toString()),
                Map.entry("bobPublic", bobPublic.toString()),
                Map.entry(
                        "attackerPublicToAlice",
                        attackerPublicToAlice.toString()
                ),
                Map.entry(
                        "attackerPublicToBob",
                        attackerPublicToBob.toString()
                ),
                Map.entry(
                        "aliceSecretWithAttacker",
                        aliceSecret.toString()
                ),
                Map.entry(
                        "attackerSecretWithAlice",
                        attackerSecretWithAlice.toString()
                ),
                Map.entry(
                        "bobSecretWithAttacker",
                        bobSecret.toString()
                ),
                Map.entry(
                        "attackerSecretWithBob",
                        attackerSecretWithBob.toString()
                ),
                Map.entry(
                        "aliceAndBobSecretsMatch",
                        aliceSecret.equals(bobSecret)
                ),
                Map.entry(
                        "attackerDerivesBothLegSecrets",
                        attackerCanReadBothLegs
                ),
                Map.entry(
                        "explanation",
                        "Without authentication, the attacker substitutes public values and establishes a separate shared secret with each party. This is a mathematical demonstration, not live network interception."
                ),
                Map.entry(
                        "warning",
                        "Use only as an educational simulator. Real protocols require authenticated key exchange."
                )
        );
    }

    public List<HistoryResponse> history() {
        return repository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(r -> new HistoryResponse(
                        r.getId(),
                        r.getMode(),
                        r.getP(),
                        r.getG(),
                        r.getAlicePublic(),
                        r.getBobPublic(),
                        r.isSecretsMatch(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    private Parameters validate(BigInteger p, BigInteger g) {
        if (p == null || g == null) {
            throw new IllegalArgumentException(
                    "Both p and g are required.");
        }

        if (p.compareTo(BigInteger.valueOf(5)) < 0
                || p.compareTo(MAX_P) > 0
                || !p.isProbablePrime(40)) {
            throw new IllegalArgumentException(
                    "p must be a prime number between 5 and 2147483647 for this educational simulator."
            );
        }

        if (g.compareTo(TWO) < 0
                || g.compareTo(p.subtract(TWO)) > 0) {
            throw new IllegalArgumentException(
                    "g must be between 2 and p - 2.");
        }

        return new Parameters(p, g);
    }

    private BigInteger privateExponent(
            BigInteger supplied,
            BigInteger p,
            String field) {

        BigInteger upper = p.subtract(TWO);

        if (supplied == null) {
            // Randomly choose an exponent from [2, p - 2].
            return randomInRange(TWO, upper);
        }

        if (supplied.compareTo(TWO) < 0
                || supplied.compareTo(upper) > 0) {
            throw new IllegalArgumentException(
                    field + " must be between 2 and p - 2.");
        }

        return supplied;
    }

    private BigInteger randomInRange(
            BigInteger min,
            BigInteger max) {

        BigInteger range =
                max.subtract(min).add(BigInteger.ONE);

        BigInteger candidate;

        do {
            candidate = new BigInteger(range.bitLength(), RANDOM);
        } while (candidate.compareTo(range) >= 0);

        return candidate.add(min);
    }

    private record Parameters(BigInteger p, BigInteger g) {
    }
}
