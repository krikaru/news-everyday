package com.example.newsapi.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.util.CookieUtils;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final AuthorizationService authorizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/user/auth") || request.getServletPath().equals("/user/token/refresh")) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            Cookie cookieRefreshToken = CookieUtils.findCookieByName(request, "refresh_token");
            String newAccessToken;

            if (authorizationHeader == null){
                newAccessToken = authorizationService.tryToGetNewAccessToken(cookieRefreshToken, response);
            } else {
                if (authorizationHeader.startsWith("Bearer_")) {
                    try {
                        String token = authorizationHeader.substring("Bearer_".length());

                        DecodedJWT decodedJWT = tokenUtils.getDecoderIfVerify(token);
                        newAccessToken = authorizationHeader;

                        authorizationService.setAuthentication(decodedJWT);
                    } catch (Exception exception) {
                        newAccessToken = authorizationService.tryToGetNewAccessToken(cookieRefreshToken, response);
                    }
                } else {
                    newAccessToken = authorizationService.tryToGetNewAccessToken(cookieRefreshToken, response);
                }
            }

            if (newAccessToken == null) {
                response.sendRedirect("http://localhost:8080/user/auth");
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
