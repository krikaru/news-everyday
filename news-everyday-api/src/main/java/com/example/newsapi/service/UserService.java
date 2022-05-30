package com.example.newsapi.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.newsapi.dto.TokensResponseDto;
import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Role;
import com.example.newsapi.repo.UserRepo;
import com.example.newsapi.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;

    public AppUser createUser(AppUser user) {
        return userRepo.save(user);
    }

    public List<AppUser> getAllUsers() {
        return userRepo.findAll();
    }

    public AppUser auth(UserRequestDto userRequestDto, HttpServletResponse response) {
        TokensResponseDto tokensResponseDto = restTemplate.postForObject(
                "http://NEWS-AUTH/api/login",
                userRequestDto,
                TokensResponseDto.class
        );

        if (tokensResponseDto == null) {
            return null;
        }

        DecodedJWT decoder = tokenUtils.getDecoderIfVerify(tokensResponseDto.getAccess_token());
        String email = decoder.getSubject();

        Optional<AppUser> userOptional = userRepo.findByEmail(email);
        AppUser user;
        if (userOptional.isEmpty()) {
            user = new AppUser();
            user.setEmail(email);
            user.setFirstName(user.getEmail().split("@")[0]);
            user.setRoles(Set.of(decoder.getClaim("roles").asArray(Role.class)));
            userRepo.save(user);
        } else {
            user = userOptional.get();
        }

        Cookie refreshCookie = new Cookie("refresh_token", "Bearer_" + tokensResponseDto.getRefresh_token());
        refreshCookie.setHttpOnly(true);

        response.addCookie(refreshCookie);
        response.setHeader("AUTHORIZATION", "Bearer_" + tokensResponseDto.getAccess_token());

        return user;
    }
}