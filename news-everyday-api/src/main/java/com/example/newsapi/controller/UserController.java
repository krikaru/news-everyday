package com.example.newsapi.controller;

import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.UserService;
import com.example.newsapi.util.TokenUtils;
import com.fasterxml.jackson.annotation.JsonView;
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
