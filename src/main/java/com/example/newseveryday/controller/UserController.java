package com.example.newseveryday.controller;

import com.example.newseveryday.model.User;
import com.example.newseveryday.model.Views;
import com.example.newseveryday.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Data
public class UserController {
    private final UserService userService;

    @PostMapping()
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @GetMapping
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }
}
