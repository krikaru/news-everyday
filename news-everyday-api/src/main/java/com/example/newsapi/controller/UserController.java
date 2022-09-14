package com.example.newsapi.controller;

import com.example.newsapi.dto.UserRequestDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('WRITER')")
    public String auth() {
        return "auth";
    }

    @PostMapping("auth")
    @JsonView(Views.ShotUser.class)
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

    @GetMapping
    @JsonView(Views.ShotUser.class)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("{id}")
    @JsonView(Views.ShotUser.class)
    public ResponseEntity<AppUser> getOneUser(@PathVariable Long id) {
        Optional<AppUser> optionalUser = userService.findById(id);

        return optionalUser.isPresent() ?
                new ResponseEntity<>(optionalUser.get(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}
