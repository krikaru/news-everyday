package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Role;
import com.example.newsapi.util.CookieUtils;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;
    private final UserService userService;

    public String tryToGetNewAccessToken(Cookie cookieRefreshToken, HttpServletResponse response) {
        TokensResponseDto tokensResponseDto = attemptToRefreshTokens(cookieRefreshToken);
        String newAccessToken = tokensResponseDto == null ? null : tokensResponseDto.getAccess_token();

        if (newAccessToken != null) {
            try {
                DecodedJWT decodedJWT = tokenUtils.getDecoderIfVerify(newAccessToken);

                setAuthentication(decodedJWT);

                response.setHeader("AUTHORIZATION", "Bearer_" + newAccessToken);
                Cookie refreshCookie = CookieUtils.createHttpOnlyCookie("refresh_token",
                        "Bearer_" + tokensResponseDto.getRefresh_token());
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

        Set<Role> authorities = new HashSet<>();
        Optional<AppUser> user = userService.findByEmail(email);
        AppUser newUser = new AppUser();
        if (user.isPresent()) {
            authorities.addAll(user.get().getRoles());
        } else {
            Arrays.stream(roles).forEach(role -> authorities.add(Role.valueOf(role)));
            newUser.setEmail(email);
            newUser.setRoles(authorities);
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.orElse(newUser), null, authorities);
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

    public AppUser getPrincipal() {
        return (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
