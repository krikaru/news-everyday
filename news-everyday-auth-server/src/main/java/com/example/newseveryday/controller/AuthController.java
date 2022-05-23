package com.example.newseveryday.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import util.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class AuthController {
    private final UserRepo userRepo;
    private final TokenUtils tokenUtils;

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());

                DecodedJWT decodedJWT = tokenUtils.getDecoder(refresh_token);
                String email = decodedJWT.getSubject();
                Optional<AppUser> user = userRepo.findByEmail(email);

                String access_token = tokenUtils.createAccessToken(user.get(), request.getRequestURL().toString());

                TokenUtils.writeTokensToResponse(access_token, refresh_token, response);
            } catch (Exception exception) {
                TokenUtils.writeErrorToResponse(response, exception);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}