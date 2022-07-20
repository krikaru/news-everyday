package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;

    public String tryToGetNewAccessToken(Cookie cookieRefreshToken, HttpServletResponse response) {
        TokensResponseDto tokensResponseDto = attemptToRefreshTokens(cookieRefreshToken);
        String newAccessToken = tokensResponseDto == null ? null : tokensResponseDto.getAccess_token();

        if (newAccessToken != null) {
            try {
                DecodedJWT decodedJWT = tokenUtils.getDecoderIfVerify(newAccessToken);

                setAuthentication(decodedJWT);

                response.setHeader("AUTHORIZATION", "Bearer_" + newAccessToken);
                Cookie refreshCookie = new Cookie("refresh_token", "Bearer_" + tokensResponseDto.getRefresh_token());
                refreshCookie.setHttpOnly(true);
                response.addCookie(refreshCookie);
                return newAccessToken;
            } catch (JWTVerificationException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setAuthentication(DecodedJWT decodedJWT) {
        String email = decodedJWT.getSubject();
        String [] roles = decodedJWT.getClaim("roles").asArray(String.class);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public TokensResponseDto attemptToRefreshTokens(Cookie cookieRefreshToken) {
        if (cookieRefreshToken != null && cookieRefreshToken.getValue().startsWith("Bearer_")){
            String refreshToken = cookieRefreshToken.getValue().substring("Bearer_".length());
            try {
                tokenUtils.getDecoderIfVerify(refreshToken);

                return getNewTokens(refreshToken);
            } catch (Exception exception) {
                return null;
            }
        } else {
            return null;
        }
    }

    public TokensResponseDto getNewTokens(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", "Bearer_" + refreshToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<TokensResponseDto> responseEntity = restTemplate.exchange(
                "http://localhost:8080/api/token/refresh",
                HttpMethod.GET, requestEntity, TokensResponseDto.class);
        return responseEntity == null ? null : responseEntity.getBody();
    }
}
