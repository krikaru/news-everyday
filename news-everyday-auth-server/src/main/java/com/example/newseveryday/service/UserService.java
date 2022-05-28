package com.example.newseveryday.service;

import com.example.newseveryday.dto.UserCreateDto;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import com.example.newseveryday.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    public AppUser create(UserCreateDto userDto) {
        AppUser user = new AppUser();

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setPassword(userDto.getPassword());
        user.setActive(true);
        user.setRoles(Set.of(Role.USER));

        return userRepo.save(user);
    }

    public Optional<AppUser> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
