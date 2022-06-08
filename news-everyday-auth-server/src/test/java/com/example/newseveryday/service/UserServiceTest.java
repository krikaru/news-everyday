package com.example.newseveryday.service;

import com.example.newseveryday.dto.UserCreateDto;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import com.example.newseveryday.rabbitmq.EmailMessageProducer;
import com.example.newseveryday.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepo userRepo;
    UserService userService;
    PasswordEncoder passwordEncoder;
    @Mock
    EmailMessageProducer emailMessageProducer;

    @BeforeEach
    void setUp() {
        passwordEncoder = NoOpPasswordEncoder.getInstance();
        userService = new UserService(userRepo, passwordEncoder, emailMessageProducer);
    }

    @Test
    void checkBuiltAppUserWhenCreate() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setEmail("test@test.ru");
        userDto.setFirstName("firstName");
        userDto.setPassword("123456");
        when(userRepo.save(any())).thenReturn(new AppUser());
        ArgumentCaptor<AppUser> capturedUser = ArgumentCaptor.forClass(AppUser.class);

        userService.create(userDto);

        verify(userRepo).save(capturedUser.capture());
        assertThat(capturedUser.getValue())
                .as("Resulting user properties wrong")
                .isEqualTo(AppUser.builder()
                        .email("test@test.ru")
                        .firstName("firstName")
                        .password("123456")
                        .roles(Set.of(Role.USER))
                        .activationCode(capturedUser.getValue().getActivationCode()) //for pass test
                        .active(false).build());

        assertThat(capturedUser.getValue().getActivationCode())
                .isNotEmpty();
    }

    @Test
    void activateUserIfExist() {
        when(userRepo.findByActivationCode(anyString())).thenReturn(Optional.of(new AppUser()));

        boolean result = userService.activateUser(anyString());

        assertThat(result)
                .as("If activation code is exist result must be true")
                .isEqualTo(true);
    }

    @Test
    void activateUserIfNotExist() {
        when(userRepo.findByActivationCode(anyString())).thenReturn(Optional.empty());

        boolean result = userService.activateUser(anyString());

        assertThat(result)
                .as("If activation code isn't exist result must be false")
                .isEqualTo(false);
    }
}