package com.example.newseveryday.service;

import com.example.newseveryday.dto.UserCreateDto;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.NotificationRequest;
import com.example.newseveryday.model.Role;
import com.example.newseveryday.rabbitmq.EmailMessageProducer;
import com.example.newseveryday.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessageProducer emailMessageProducer;

    public AppUser create(UserCreateDto userDto) {

        AppUser user = new AppUser();

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setActive(false);
        user.setRoles(Set.of(Role.USER));

        AppUser savedUser = userRepo.save(user);

        sendConfirmToUser(savedUser);

        return savedUser;
    }

    private void sendConfirmToUser(AppUser user) {
        NotificationRequest notificationRequest = new NotificationRequest(
                user.getEmail(),
                "Confirm your account",
                String.format("Hi %s, welcome to News Every Day. Please visit next link: http://localhost:8080/api/user/activate/%s",
                        user.getFirstName(),
                        user.getActivationCode())
        );
        emailMessageProducer.sendToBroker(notificationRequest);
    }

    public Optional<AppUser> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }


    public boolean activateUser(String activationCode) {
        Optional<AppUser> optUser = userRepo.findByActivationCode(activationCode);

        if (optUser.isPresent()) {
            AppUser appUser = optUser.get();
            appUser.setActivationCode(null);
            appUser.setActive(true);
            userRepo.save(appUser);
            return true;
        }
        return false;
    }
}
