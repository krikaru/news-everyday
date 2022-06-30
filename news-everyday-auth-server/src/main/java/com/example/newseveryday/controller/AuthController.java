package com.example.newseveryday.controller;

import com.example.newseveryday.service.AuthService;
import com.example.newseveryday.util.TokenUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/api/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer_")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer_".length());

                String access_token = authService.createAccessToken(request, refresh_token);

                TokenUtils.writeTokensToResponse(access_token, refresh_token, response);
            } catch (Exception exception) {
                TokenUtils.writeErrorToResponse(response, exception);
            }
        } else {
            TokenUtils.writeErrorToResponse(response, new RuntimeException("Refresh token is missing or having wrong format"));
        }
    }
}
