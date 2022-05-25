package com.example.newseveryday.controller;

import com.example.newseveryday.dto.UserRequestDto;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.model.Views;
import com.example.newseveryday.service.UserService;
import com.example.newseveryday.util.TokenUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final TokenUtils tokenUtils;

    @GetMapping("/test")
    public String auth() {
        return "auth";
    }

    @PostMapping("auth")
    public ResponseEntity<AppUser> auth(
            @RequestBody UserRequestDto userRequestDto,
            HttpServletResponse response
    ) {
        AppUser user = userService.auth(userRequestDto, response);
        if (user == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/create")
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<AppUser> createUser(@RequestBody AppUser user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @GetMapping
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }
}
