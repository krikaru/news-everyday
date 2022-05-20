package com.example.newseveryday.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Views;
import com.example.newseveryday.service.UserService;
import com.example.newseveryday.util.TokenUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/user")
@Data
public class UserController {
    private final UserService userService;
    private final TokenUtils tokenUtils;

    @PostMapping("/create")
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<AppUser> createUser(@RequestBody AppUser user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @GetMapping
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());

                DecodedJWT decodedJWT = tokenUtils.getDecoder(refresh_token);
                String email = decodedJWT.getSubject();
                AppUser user = userService.getUserByEmail(email);

                String access_token = tokenUtils.createAccessToken(user, request.getRequestURL().toString());

                TokenUtils.writeTokensToResponse(access_token, refresh_token, response);
            } catch (Exception exception) {
                TokenUtils.writeErrorToResponse(response, exception);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}
