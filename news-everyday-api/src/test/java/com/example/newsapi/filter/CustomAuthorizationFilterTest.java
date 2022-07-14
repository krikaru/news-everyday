package com.example.newsapi.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.util.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
//import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthorizationFilterTest {
    CustomAuthorizationFilter customAuthorizationFilter;
    MockHttpServletRequest request;
    @Mock
    MockHttpServletResponse response;
    @Mock
    FilterChain filterChain;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    DecodedJWT decodedJWT;
    @Mock
    AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        customAuthorizationFilter = new CustomAuthorizationFilter(tokenUtils, authorizationService);
        request = new MockHttpServletRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/auth", "/user/token/refresh"})
    void doFilterInternal_doFilterIfServletPathIsPermit(String path) throws IOException, ServletException {
        request.setServletPath(path);
        customAuthorizationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doFilterWhenAccessTokenIsValid() throws Exception {
        request.setServletPath("not_permission_path");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer_valid_token");
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(decodedJWT);

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessAndRefreshTokenIsNull() throws Exception {
        request.setServletPath("not_permission_path");
        customAuthorizationFilter.doFilterInternal(request, response, filterChain);
        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessTokenNotValid() throws Exception {
        request.setServletPath("not_permission_path");
        request.addHeader(HttpHeaders.AUTHORIZATION, "not_valid_token");

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessTokenIsNullAndRefreshTokenHasNotStartsWithBearer() throws Exception {
        request.setServletPath("not_permission_path");
        request.setCookies(new Cookie("refresh_token", "wrong_format_token"));

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessTokenIsNullAndRefreshTokenNotValid() throws Exception {
        request.setServletPath("not_permission_path");
        request.setCookies(new Cookie("refresh_token", "Bearer_not_valid_token"));
        when(authorizationService.tryToGetNewAccessToken(any(), any())).thenReturn(null);

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }

    @Test
    void doFilterInternal_doFilterWhenAccessTokenIsNullAndRefreshTokenValid() throws Exception {
        request.setServletPath("not_permission_path");
        when(authorizationService.tryToGetNewAccessToken(any(), any())).thenReturn("valid_token");

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessTokenNotStartsWithBearerAndRefreshTokenIsNull() throws Exception {
        request.setServletPath("not_permission_path");
        request.addHeader(HttpHeaders.AUTHORIZATION, "not_start_with_bearer");

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }

    @Test
    void doFilterInternal_sendRedirectWhenAccessTokenNotValidAndRefreshTokenIsNull() throws Exception {
        request.setServletPath("not_permission_path");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer_not_valid_token");
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());
        when(authorizationService.tryToGetNewAccessToken(any(), any())).thenReturn(null);

        customAuthorizationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendRedirect("http://localhost:8080/user/auth");
    }
}