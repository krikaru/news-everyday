package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Role;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @Mock
    TokensResponseDto tokensResponseDto;
    @Mock
    UserService userService;
    @Captor
    ArgumentCaptor<HttpEntity<Void>> httpEntity;

    @BeforeEach
    void setUp() {
        authorizationService = spy(new AuthorizationService(tokenUtils, restTemplate, userService));
        response = new MockHttpServletResponse();
    }

    @Test
    void tryToGetNewAccessToken_returnNullIfNewAccessTokenIsNull() {
        Cookie cookie = new Cookie("refresh_token", "any");
        doReturn(tokensResponseDto).when(authorizationService).attemptToRefreshTokens(any());
        when(tokensResponseDto.getAccess_token()).thenReturn(null);

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isNull();
    }

    @Test
    void tryToGetNewAccessToken_returnNullIfNewAccessTokenNotValid() {
        Cookie cookie = new Cookie("refresh_token", "any");
        doReturn(tokensResponseDto).when(authorizationService).attemptToRefreshTokens(any());
        when(tokensResponseDto.getAccess_token()).thenReturn("not_valid");
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isNull();
    }

    @Test
    void tryToGetNewAccessToken_returnNewAccessTokenIfNewAccessTokenValid() {
        Cookie cookie = new Cookie("refresh_token", "any");
        String newAccessToken = "any";
        doReturn(tokensResponseDto).when(authorizationService).attemptToRefreshTokens(any());
        when(tokensResponseDto.getAccess_token()).thenReturn(newAccessToken);
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(any());

        String result = authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(result).isEqualTo(newAccessToken);
    }

    @Test
    void tryToGetNewAccessToken_checkFillResponseHeaderAndCookie() {
        Cookie cookie = new Cookie("refresh_token", "any");
        String newAccessToken = "any";
        String newRefreshToken = "any";
        doReturn(tokensResponseDto).when(authorizationService).attemptToRefreshTokens(any());
        when(tokensResponseDto.getAccess_token()).thenReturn(newAccessToken);
        when(tokensResponseDto.getRefresh_token()).thenReturn(newRefreshToken);
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        doNothing().when(authorizationService).setAuthentication(any());

        authorizationService.tryToGetNewAccessToken(cookie, response);

        assertThat(response.getHeader("AUTHORIZATION")).isEqualTo("Bearer_" + newAccessToken);
        assertThat(Objects.requireNonNull(response.getCookie("refresh_token")).getValue()).isEqualTo("Bearer_" + newRefreshToken);
        assertTrue(Objects.requireNonNull(response.getCookie("refresh_token")).isHttpOnly());
    }

    @Test
    @WithMockUser
    void setAuthentication_checkFillAuthenticationObjectIfUserAlreadyExistInDb() {
        when(decodedJWT.getSubject()).thenReturn("subject@test.ru");
        when(decodedJWT.getClaim("roles")).thenReturn(claim);
        when(claim.asArray(String.class)).thenReturn(new String[]{"USER"});
        AppUser appUser = new AppUser();
        appUser.setEmail("subject@test.ru");
        appUser.setRoles(Set.of(Role.USER, Role.ADMIN));
        Optional<AppUser> userFromDb = Optional.of(appUser);
        when(userService.findByEmail(anyString())).thenReturn(userFromDb);

        authorizationService.setAuthentication(decodedJWT);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        AppUser principal = (AppUser) result.getPrincipal();
        assertThat(principal.getEmail()).isEqualTo("subject@test.ru");
        assertTrue(result.getAuthorities().contains(Role.USER));
        assertTrue(result.getAuthorities().contains(Role.ADMIN));

    }

    @Test
    @WithMockUser
    void setAuthentication_checkFillAuthenticationObjectIfUserNotExistInDbYet() {
        when(decodedJWT.getSubject()).thenReturn("subject@test.ru");
        when(decodedJWT.getClaim("roles")).thenReturn(claim);
        when(claim.asArray(String.class)).thenReturn(new String[]{"USER"});
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

        authorizationService.setAuthentication(decodedJWT);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        AppUser principal = (AppUser) result.getPrincipal();
        assertThat(principal.getEmail()).isEqualTo("subject@test.ru");
        assertTrue(result.getAuthorities().contains(Role.USER));
        assertFalse(result.getAuthorities().contains(Role.ADMIN));
    }

    @Test
    void attemptToRefreshTokens_returnNullIfCookieIsNull() {
        TokensResponseDto result = authorizationService.attemptToRefreshTokens(null);
        assertThat(result).isNull();
    }

    @Test
    void attemptToRefreshTokens_returnNullIfCookieNotStartsWithBearer() {
        Cookie cookie = new Cookie("refresh_token", "not_start_with_bearer");
        TokensResponseDto result = authorizationService.attemptToRefreshTokens(cookie);
        assertThat(result).isNull();
    }

    @Test
    void attemptToRefreshTokens_returnNullIfRefreshTokenNotValid() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_not_valid");
        doThrow(JWTVerificationException.class).when(tokenUtils).getDecoderIfVerify(anyString());

        TokensResponseDto result = authorizationService.attemptToRefreshTokens(cookie);

        assertThat(result).isNull();
    }

    @Test
    void attemptToRefreshTokens_returnNullIfResponseWithNewTokenIsNull() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_valid");
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        Mockito.doReturn(null).when(authorizationService).getNewTokens(anyString());

        TokensResponseDto result = authorizationService.attemptToRefreshTokens(cookie);

        assertThat(result).isNull();
        assertThatCode(() -> verify(authorizationService).getNewTokens(anyString())).doesNotThrowAnyException();
    }

    @Test
    void attemptToRefreshTokens_returnAccessTokenIfRefreshTokenIsValid() {
        Cookie cookie = new Cookie("refresh_token", "Bearer_valid");
        TokensResponseDto tokensResponseDto = new TokensResponseDto();
        tokensResponseDto.setAccess_token("new_access_token");
        when(tokenUtils.getDecoderIfVerify(anyString())).thenReturn(decodedJWT);
        Mockito.doReturn(tokensResponseDto).when(authorizationService).getNewTokens(anyString());

        TokensResponseDto result = authorizationService.attemptToRefreshTokens(cookie);

        assertThat(result).isNotNull();
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