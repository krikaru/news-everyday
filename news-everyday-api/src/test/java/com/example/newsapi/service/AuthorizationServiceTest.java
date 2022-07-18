package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.util.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    AuthorizationService authorizationService;
    MockHttpServletResponse response;
    @Mock
    TokenUtils tokenUtils;
    @Mock
    RestTemplate restTemplate;
    @Mock
    DecodedJWT decodedJWT;
    @Mock
    Claim claim;
    @Captor
    ArgumentCaptor<HttpEntity<Void>> httpEntity;

    @BeforeEach
    void setUp() {
        authorizationService = spy(new AuthorizationService(tokenUtils, restTemplate));
        response = new MockHttpServletResponse();
    }

    @Test
    void tryToGetNewAccessToken_returnNullIfNewAccessTokenIsNull() {
        Cookie cookie = new Cookie("refresh_token", "any");
        doReturn(null).when(authorizationService).getAccessTokenFromCookie(any());

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isNull();
    }

    @Test
    void tryToGetNewAccessToken_returnNullIfNewAccessTokenNotValid() {
        Cookie cookie = new Cookie("refresh_token", "any");
        doReturn("any").when(authorizationService).getAccessTokenFromCookie(any());
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isNull();
    }

    @Test
    void tryToGetNewAccessToken_returnNewAccessTokenIfNewAccessTokenValid() {
        Cookie cookie = new Cookie("refresh_token", "any");
        String newAccessToken = "any";
        doReturn(newAccessToken).when(authorizationService).getAccessTokenFromCookie(any());
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(any());

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isEqualTo(newAccessToken);
    }

    @Test
    void tryToGetNewAccessToken_checkFillResponseHeader() {
        Cookie cookie = new Cookie("refresh_token", "any");
        String newAccessToken = "any";
        doReturn(newAccessToken).when(authorizationService).getAccessTokenFromCookie(any());
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(any());

        authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(response.getHeader("AUTHORIZATION")).isEqualTo("Bearer_" + newAccessToken);
    }

    @Test
    @WithMockUser
    void setAuthentication_checkFillAuthenticationObject() {
        when(decodedJWT.getSubject()).thenReturn("subject@test.ru");
        when(decodedJWT.getClaim("roles")).thenReturn(claim);
        when(claim.asArray(String.class)).thenReturn(new String[]{"USER", "ADMIN"});

        authorizationService.setAuthentication(decodedJWT);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result.getPrincipal()).isEqualTo("subject@test.ru");
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));

    }

    @Test
    void getAccessTokenFromCookie_returnNullIfCookieIsNull() {
        String result = authorizationService.getAccessTokenFromCookie(null);
        assertThat(result).isNull();
    }

    @Test
    void getAccessTokenFromCookie_returnNullIfCookieNotStartsWithBearer() {
        Cookie cookie = new Cookie("refresh_token", "not_start_with_bearer");
        String result = authorizationService.getAccessTokenFromCookie(cookie);
        assertThat(result).isNull();
    }

    @Test
    void getAccessTokenFromCookie_returnNullIfRefreshTokenNotValid() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_not_valid");
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());

        String result = authorizationService.getAccessTokenFromCookie(cookie);

        assertThat(result).isNull();
    }

    @Test
    void getAccessTokenFromCookie_returnNullIfResponseWithNewTokenIsNull() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_valid");
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        Mockito.doReturn(null).when(authorizationService).getNewTokens(anyString());

        String result = authorizationService.getAccessTokenFromCookie(cookie);

        assertThat(result).isNull();
        assertThatCode(() -> verify(authorizationService).getNewTokens(anyString())).doesNotThrowAnyException();
    }

    @Test
    void getAccessTokenFromCookie_returnAccessTokenIfRefreshTokenIsValid() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_valid");
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token("new_access_token");
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        Mockito.doReturn(tokensResponseDto).when(authorizationService).getNewTokens(anyString());

        String result = authorizationService.getAccessTokenFromCookie(cookie);

        assertThat(result).isEqualTo(tokensResponseDto.getAccess_token());
    }

    @Test
    void getNewTokens_checkHeaderInRequestEntity() {
        String refreshToken = "refresh_token";

        authorizationService.getNewTokens(refreshToken);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), httpEntity.capture(), any(Class.class));
        assertThat(httpEntity.getValue().getHeaders().get("AUTHORIZATION").get(0)).isEqualTo("Bearer_" + refreshToken);
    }

    @Test
    void getNewTokens_returnNullIfResponseEntityIsNull() {
        String refreshToken = "refresh_token";
        when(restTemplate.exchange(anyString(), any(), any(), any(Class.class))).thenReturn(null);

        TokensResponseDto result = authorizationService.getNewTokens(refreshToken);

        assertThat(result).isNull();
    }

    @Test
    void getNewTokens_returnDtoIfResponseEntityIsNull() {
        String refreshToken = "refresh_token";
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token("new_access_token");
        ResponseEntity<TokensResponseDto> responseEntity = new ResponseEntity<>(tokensResponseDto, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(Class.class))).thenReturn(responseEntity);

        TokensResponseDto result = authorizationService.getNewTokens(refreshToken);

        assertThat(result.getAccess_token()).isEqualTo(tokensResponseDto.getAccess_token());
    }
}