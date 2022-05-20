package com.example.newseveryday.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TokenUtils {
    private Algorithm algorithm;

    public TokenUtils(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public DecodedJWT getDecoder(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String createAccessToken(AppUser user, String issuer) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withIssuer(issuer)
                .withClaim("roles", user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public static void writeTokensToResponse(String access_token, String refresh_token, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    public static void writeErrorToResponse(HttpServletResponse response, Exception exception) throws IOException {
        response.setHeader("error", exception.getMessage());
        response.setStatus(FORBIDDEN.value());
        response.setContentType(APPLICATION_JSON_VALUE);

        Map<String, String> error = new HashMap<>();
        error.put("error_message", exception.getMessage());

        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

    public String createRefreshToken(AppUser user, String issuer) {
        //        response.setHeader("access_token", access_token);
        //        response.setHeader("refresh_token", refresh_token);
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 *1000))
                .withIssuer(issuer)
                .sign(algorithm);
    }
}
