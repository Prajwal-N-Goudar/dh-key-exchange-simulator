package com.cns.dh.dto;

import java.math.BigInteger;

public record MitmRequest(
        BigInteger p,
        BigInteger g,
        BigInteger alicePrivate,
        BigInteger bobPrivate,
        BigInteger attackerPrivateForAlice,
        BigInteger attackerPrivateForBob
) {}
