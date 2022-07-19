package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Role;
import com.example.newsapi.repo.UserRepo;
import com.example.newsapi.util.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    UserService userService;
    @Mock
    UserRepo userRepo;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    RestTemplate restTemplate;
    @Mock
    AuthorizationService authorizationService;
    @Mock
    DecodedJWT decodedJWT;
    @Mock
    Claim claim;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        userService = spy(new UserService(userRepo, tokenUtils, restTemplate, authorizationService));
        response = new MockHttpServletResponse();
    }

    @Test
    void auth_returnNullIfTokensResponseDtoIsNull() {
        when(restTemplate.postForObject(anyString(), any(), any(Class.class))).thenReturn(null);
        AppUser result = userService.auth(new UserRequestDto(), response);
        assertThat(result).isNull();
    }

    @Test
    void auth_returnNullIfAccessTokenNotValid() {
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token("any");
        when(restTemplate.postForObject(anyString(), any(), any(Class.class))).thenReturn(tokensResponseDto);
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());

        AppUser result = userService.auth(new UserRequestDto(), response);

        assertThat(result).isNull();
    }

    @Test
    void auth_returnUserIfAccessTokenValid() {
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token("valid_token");
        when(restTemplate.postForObject(anyString(), any(), any(Class.class))).thenReturn(tokensResponseDto);
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doReturn(new AppUser()).when(userService).extractUser(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(decodedJWT);

        AppUser result = userService.auth(new UserRequestDto(), response);

        assertThat(result).isNotNull();
    }

    @Test
    void auth_checkFillResponseCookieAndHeader() {
        String accessToken = "valid_access_token";
        String refreshToken = "valid_refresh_token";
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token(accessToken);
        tokensResponseDto.setRefresh_token(refreshToken);
        when(restTemplate.postForObject(anyString(), any(), any(Class.class))).thenReturn(tokensResponseDto);
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doReturn(new AppUser()).when(userService).extractUser(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(decodedJWT);

        userService.auth(new UserRequestDto(), response);

        assertThat(Objects.requireNonNull(response.getCookie("refresh_token")).getValue())
                .isEqualTo("Bearer_" + refreshToken);
        assertTrue(Objects.requireNonNull(response.getCookie("refresh_token")).isHttpOnly());
        assertThat(response.getHeader("AUTHORIZATION")).isEqualTo("Bearer_" + accessToken);
    }

    @Test
    void extractUser_verifySaveToDbMethodIsCalled() {
        String email = "test@test.ru";
        Role[] roles = {Role.USER, Role.ADMIN};
        when(decodedJWT.getSubject()).thenReturn(email);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(decodedJWT.getClaim("roles")).thenReturn(claim);
        when(claim.asArray(Role.class)).thenReturn(roles);
        when(userRepo.save(any())).thenReturn(new AppUser());

        userService.extractUser(decodedJWT);

        verify(userRepo).save(any());
    }
}