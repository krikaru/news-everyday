package com.example.newseveryday.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.newseveryday.repo.UserRepo;
import com.example.newseveryday.service.AuthService;
import com.example.newseveryday.service.UserService;
import com.example.newseveryday.util.TokenUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    UserRepo userRepo;
    @MockBean
    AuthService authService;
    MockedStatic<TokenUtils> staticTokenUtils;

    ArgumentCaptor<RuntimeException> capturedException;

    @BeforeEach
    void setUp() {
        this.staticTokenUtils = Mockito.mockStatic(TokenUtils.class);
        this.capturedException = ArgumentCaptor.forClass(RuntimeException.class);
    }

    @AfterEach
    void tearDown() {
        this.staticTokenUtils.close();
    }

    @Test
    void refreshToken_respondingErrorWhenHeaderAuthorizationIsNotValid() throws Exception {
        when(authService.createAccessToken(any(), any())).thenThrow(JWTVerificationException.class);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer_wrongFormat"));

        staticTokenUtils.verify(() -> TokenUtils.writeErrorToResponse(any(HttpServletResponse.class), any()),
                description("Header isn't valid therefore must be invoked writeErrorToResponse()"));
    }

    @Test
    void refreshToken_respondingErrorWhenHeaderAuthorizationIsNull() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh"));


        staticTokenUtils.verify(() -> TokenUtils.writeErrorToResponse(any(HttpServletResponse.class), capturedException.capture()),
                description("Header is null therefore must be invoked writeErrorToResponse()"));
    }

    @Test
    void refreshToken_respondingErrorWhenHeaderAuthorizationNotStartsWithBearer() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh")
                .header(HttpHeaders.AUTHORIZATION, "wrongFormat"));


        staticTokenUtils.verify(() -> TokenUtils.writeErrorToResponse(any(HttpServletResponse.class), capturedException.capture()),
                description("Header isn't start with \"Bearer_\" therefore must be invoked writeErrorToResponse()"));
    }

    @Test
    void refreshToken_respondingTokensWhenHeaderAuthorizationIsValid() throws Throwable {
        when(authService.createAccessToken(any(), any())).thenReturn("Bearer_rightFormat");

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer_rightFormat"));

        staticTokenUtils.verify(() -> TokenUtils.writeTokensToResponse(anyString(), anyString(), any(HttpServletResponse.class)),
                description("Header is valid therefore must be invoked writeTokensToResponse()"));
    }
}