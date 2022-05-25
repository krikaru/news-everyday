package com.example.newseveryday.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class TokenUtils {
    private final Algorithm algorithm;

    public TokenUtils(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public DecodedJWT getDecoderIfVerify(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}
