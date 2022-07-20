package com.example.newsapi.controller;

import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    UserController userController;
    MockHttpServletResponse response;

    @Mock
    UserService userService;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        response = new MockHttpServletResponse();
    }

    @Test
    void auth_returnOkAndBodyIfAuthSuccess() {
        UserRequestDto userRequestDto = new UserRequestDto();
        when(userService.auth(any(), any())).thenReturn(new AppUser());

        ResponseEntity<AppUser> result = userController.auth(userRequestDto, response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void auth_returnBadRequestIfAuthFail() {
        UserRequestDto userRequestDto = new UserRequestDto();
        when(userService.auth(any(), any())).thenReturn(null);

        ResponseEntity<AppUser> result = userController.auth(userRequestDto, response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNull();
    }
}