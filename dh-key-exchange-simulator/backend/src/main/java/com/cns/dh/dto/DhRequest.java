package com.cns.dh.dto;

import java.math.BigInteger;

public record DhRequest(
        BigInteger p,
        BigInteger g,
        BigInteger alicePrivate,
        BigInteger bobPrivate
) {}
