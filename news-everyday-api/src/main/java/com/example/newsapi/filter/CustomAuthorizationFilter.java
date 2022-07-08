package com.example.newsapi.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.util.CookieUtils;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/user/auth") || request.getServletPath().equals("/user/token/refresh")) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            Cookie cookieRefreshToken = CookieUtils.findCookieByName(request, "refresh_token");
            String newAccessToken = null;

            if (authorizationHeader == null){
                newAccessToken = tryToGetNewAccessToken(cookieRefreshToken, response);
            } else {
                if (authorizationHeader.startsWith("Bearer_")) {
                    try {
                        String token = authorizationHeader.substring("Bearer_".length());

                        DecodedJWT decodedJWT = tokenUtils.getDecoderIfVerify(token);

                        setAuthentication(decodedJWT);

                        filterChain.doFilter(request, response);
                    } catch (Exception exception) {
                        newAccessToken = tryToGetNewAccessToken(cookieRefreshToken, response);
                    }
                }
            }

            if (newAccessToken == null) {
                response.sendRedirect("http://localhost:8080/user/auth");
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    private String tryToGetNewAccessToken(Cookie cookieRefreshToken, HttpServletResponse response) {
        String newAccessToken = getAccessTokenFromCookie(cookieRefreshToken);

        if (newAccessToken != null) {

            DecodedJWT decodedJWT = tokenUtils.getDecoderIfVerify(newAccessToken);

            setAuthentication(decodedJWT);

            response.setHeader("AUTHORIZATION", "Bearer_" + newAccessToken);

            return newAccessToken;
        } else {
            return null;
        }
    }

    private void setAuthentication(DecodedJWT decodedJWT) {
        String email = decodedJWT.getSubject();
        String [] roles = decodedJWT.getClaim("roles").asArray(String.class);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getAccessTokenFromCookie(Cookie cookieRefreshToken) {
        if (cookieRefreshToken != null){
            String refreshToken = cookieRefreshToken.getValue().substring("Bearer_".length());
            try {
                tokenUtils.getDecoderIfVerify(refreshToken);

                TokensResponseDto tokensResponseDto = getNewTokens(refreshToken);

                return tokensResponseDto != null ? tokensResponseDto.getAccess_token() : null;
            } catch (Exception exception) {
                return null;
            }
        } else {
            return null;
        }
    }

    private TokensResponseDto getNewTokens(String refreshToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", "Bearer_" + refreshToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<TokensResponseDto> tokensResponseDto = restTemplate.exchange(
                "http://localhost:8080/api/token/refresh",
                HttpMethod.GET, requestEntity, TokensResponseDto.class);
        return tokensResponseDto.getBody();
    }
}
