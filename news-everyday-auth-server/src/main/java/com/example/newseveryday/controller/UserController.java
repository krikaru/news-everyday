package com.example.newseveryday.controller;

import com.example.newseveryday.dto.UserCreateDto;
import com.example.newseveryday.model.Views;
import com.example.newseveryday.service.UserService;
import com.example.newseveryday.util.BindingResultUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("create")
    @JsonView(Views.ShortInfo.class)
    public ResponseEntity<UserCreateDto> create(
            @RequestBody @Valid UserCreateDto userDto,
            BindingResult bindingResult
    ) {
        if (userDto.getPassword().equals(userDto.getConfirmPassword())) {
            BindingResultUtils.addErrors(bindingResult, "confirmPassword",
                    "Пароли не совпадают!");
        }
        if (userService.findByEmail(userDto.getEmail()).isPresent()) {
            BindingResultUtils.addErrors(bindingResult, "email",
                    "Пользователь с таким email существует.");
        }

        if (!bindingResult.hasErrors()) {
            return userService.create(userDto) == null ?
                    new ResponseEntity<>(HttpStatus.BAD_REQUEST) :
                    new ResponseEntity<>(userDto, HttpStatus.CREATED);
        } else {
            userDto.setErrors(BindingResultUtils.getErrors(bindingResult));
            return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
        }

    }
}
