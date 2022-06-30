package com.example.newseveryday.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.util.TokenUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenUtils tokenUtils;

    public String createAccessToken(HttpServletRequest request, String refresh_token) throws JWTVerificationException {
        DecodedJWT decodedJWT = tokenUtils.getDecoder(refresh_token);
        String email = decodedJWT.getSubject();

        Optional<AppUser> optUser = userService.findByEmail(email);
        AppUser user = optUser.orElseThrow(() -> new NullPointerException("User not found"));

        String access_token = tokenUtils.createAccessToken(user, request.getRequestURL().toString());
        return access_token;
    }
}
