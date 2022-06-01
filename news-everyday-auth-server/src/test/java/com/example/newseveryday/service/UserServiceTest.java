package com.example.newseveryday.service;

import com.example.newseveryday.dto.UserCreateDto;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Role;
import com.example.newseveryday.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    UserRepo userRepo;
    UserService userService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        userService = new UserService(userRepo);
    }

    @Test
    void checkBuiltAppUser() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setEmail("test@test.ru");
        userDto.setFirstName("firstName");
        userDto.setPassword("123456");
        when(userRepo.save(any())).thenReturn(any());
        ArgumentCaptor<AppUser> capturedUser = ArgumentCaptor.forClass(AppUser.class);

        userService.create(userDto);

        verify(userRepo).save(capturedUser.capture());
        assertThat(capturedUser.getValue())
                .as("User properties wrong")
                .isEqualTo(AppUser.builder()
                        .email("test@test.ru")
                        .firstName("firstName")
                        .password("123456")
                        .roles(Set.of(Role.USER))
                        .active(true).build());

    }
}