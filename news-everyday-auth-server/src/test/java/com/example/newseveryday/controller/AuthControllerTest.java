package com.example.newseveryday.controller;

import com.auth0.jwt.JWT;
import com.example.newseveryday.repo.UserRepo;
import com.example.newseveryday.service.UserService;
import com.example.newseveryday.util.TokenUtils;
import org.apache.http.HttpHeaders;
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
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    UserRepo userRepo;

    MockedStatic<TokenUtils> staticTokenUtils;
    MockedStatic<Calendar> staticCalendar;

    MockedStatic<JWT> jwt;
    ArgumentCaptor<RuntimeException> capturedException;
    @BeforeEach
    void setUp() {
        this.staticCalendar = mockStatic(Calendar.class);
        this.jwt = mockStatic(JWT.class);
        this.staticTokenUtils = Mockito.mockStatic(TokenUtils.class);
        this.capturedException = ArgumentCaptor.forClass(RuntimeException.class);
    }

    @Test
    void respondingErrorWhenHeaderAuthorizationHaveWrongFormat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer_wrongFormat"));

        staticTokenUtils.verify(() -> TokenUtils.writeErrorToResponse(any(HttpServletResponse.class), capturedException.capture()));
        assertThat(capturedException.getValue().getMessage())
                .as("Exception message wrong!")
                .isEqualTo("Token isn't valid");
    }

    @Test
    void respondingErrorWhenHeaderAuthorizationIsNull() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/token/refresh"));


        staticTokenUtils.verify(() -> TokenUtils.writeErrorToResponse(any(HttpServletResponse.class), capturedException.capture()),
                description("Header is null -> must be invoked writeErrorToResponse()"));
        assertThat(capturedException.getValue().getMessage())
                .as("Exception message wrong!")
                .isEqualTo("Refresh token is missing");
    }
}