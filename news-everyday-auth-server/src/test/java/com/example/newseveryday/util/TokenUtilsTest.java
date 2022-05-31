package com.example.newseveryday.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenUtilsTest {
    static Algorithm algorithm;
    static TokenUtils tokenUtils;
    static JWTVerifier verifier;

    @BeforeAll
    static void beforeAll() {
        algorithm = Algorithm.HMAC256("test".getBytes());
        tokenUtils = new TokenUtils(algorithm);
        verifier = JWT.require(algorithm).build();
    }

    @Test
    void checkCreatingAccessTokenIsCorrect() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@test.ru");
        appUser.setRoles(Set.of(Role.USER, Role.ADMIN));
        String issuer = "issuer";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30);

        String resultToken = tokenUtils.createAccessToken(appUser, issuer);

        DecodedJWT decodedToken = verifier.verify(resultToken);
        assertThat(decodedToken.getClaim("roles").asList(Role.class))
                .as("There are must list of roles")
                .containsExactly(Role.USER, Role.ADMIN);
        assertThat(decodedToken.getSubject())
                .as("Email must be test@test.ru")
                .isEqualTo("test@test.ru");
        assertThat(decodedToken.getIssuer())
                .isEqualTo("issuer");
        assertThat(decodedToken.getExpiresAt())
                .as("Time isn't correct")
                .isEqualToIgnoringSeconds(cal.getTime());

    }

    @SneakyThrows
    @Test
    void checkWritingTokenInfoToResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String accessToken = "access token";
        String refreshToken = "refresh token";

        TokenUtils.writeTokensToResponse(accessToken, refreshToken, response);

        assertThat(response.getContentType())
                .as("Content type is wrong")
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @SneakyThrows
    @Test
    void checkWritingErrorInfoToResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        TokenUtils.writeErrorToResponse(response, new RuntimeException("some exception"));

        assertThat(response.getContentType())
                .as("Content type is wrong")
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getHeader("error"))
                .as("Must be header with name error")
                .isEqualTo("some exception");
        assertThat(response.getStatus())
                .as("Status must be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void checkCreatingRefreshTokenIsCorrect() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@test.ru");
        String issuer = "issuer";
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);

        String resultToken = tokenUtils.createRefreshToken(appUser, issuer);

        DecodedJWT decodedToken = verifier.verify(resultToken);
        assertThat(decodedToken.getSubject())
                .as("Email must be test@test.ru")
                .isEqualTo("test@test.ru");
        assertThat(decodedToken.getIssuer())
                .isEqualTo("issuer");
        assertThat(decodedToken.getExpiresAt())
                .as("Time isn't correct")
                .isEqualToIgnoringHours(cal.getTime());
    }
}