package com.example.newseveryday.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenUtils {
    private final Algorithm algorithm;

    public TokenUtils(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public DecodedJWT getDecoder(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String createAccessToken(AppUser user, String issuer) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30);

        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(cal.getTime())
                .withIssuer(issuer)
                .withClaim("roles", user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public static void writeTokensToResponse(String access_token, String refresh_token, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    public static void writeErrorToResponse(HttpServletResponse response, Exception exception) throws IOException {
        response.setHeader("error", exception.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> error = new HashMap<>();
        error.put("error_message", exception.getMessage());

        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

    public String createRefreshToken(AppUser user, String issuer) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);

        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(cal.getTime())
                .withIssuer(issuer)
                .sign(algorithm);
    }
}
