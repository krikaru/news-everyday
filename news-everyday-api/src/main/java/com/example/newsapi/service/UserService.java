package com.example.newsapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Role;
import com.example.newsapi.repo.UserRepo;
import com.example.newsapi.util.CookieUtils;
import com.example.newsapi.util.TokenUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;
    private final AuthorizationService authorizationService;

    public UserService(UserRepo userRepo, TokenUtils tokenUtils,
                       RestTemplate restTemplate, @Lazy AuthorizationService authorizationService) {
        this.userRepo = userRepo;
        this.tokenUtils = tokenUtils;
        this.restTemplate = restTemplate;
        this.authorizationService = authorizationService;
    }


//    public AppUser createUser(AppUser user) {
//        return userRepo.save(user);
//    }
//
//    public List<AppUser> getAllUsers() {
//        return userRepo.findAll();
//    }

    public AppUser auth(UserRequestDto userRequestDto, HttpServletResponse response) {
        TokensResponseDto tokensResponseDto = restTemplate.postForObject(
                "http://localhost:8080/api/login",
                userRequestDto,
                TokensResponseDto.class
        );

        if (tokensResponseDto == null) return null;

        DecodedJWT decoder;
        try {
            decoder = tokenUtils.getDecoderIfVerify(tokensResponseDto.getAccess_token());
        } catch (JWTVerificationException e) {
            return null;
        }

        AppUser user = extractUser(decoder);
        authorizationService.setAuthentication(decoder);

        Cookie refreshCookie = CookieUtils.createHttpOnlyCookie(
                "refresh_token", "Bearer_" + tokensResponseDto.getRefresh_token());

        response.addCookie(refreshCookie);
        response.setHeader("AUTHORIZATION", "Bearer_" + tokensResponseDto.getAccess_token());
        return user;
    }

    public AppUser extractUser(DecodedJWT decoder) {
        String email = decoder.getSubject();

        Optional<AppUser> userOptional = userRepo.findByEmail(email);
        AppUser user;
        if (userOptional.isEmpty()) {
            user = new AppUser();
            user.setEmail(email);
            user.setFirstName(user.getEmail().split("@")[0]);
            user.setRoles(Set.of(decoder.getClaim("roles").asArray(Role.class)));
            user = userRepo.save(user);
        } else {
            user = userOptional.get();
        }
        return user;
    }

    public Optional<AppUser> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
