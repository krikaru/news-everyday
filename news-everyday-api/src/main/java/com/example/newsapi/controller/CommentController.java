package com.example.newsapi.controller;

import com.example.newsapi.dto.CommentResponseDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Comment;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.service.CommentService;
import com.example.newsapi.util.BindingResultUtils;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @JsonView(Views.ShortNews.class)
    public List<Comment> getAll() {
        return commentService.findAll();
    }

    @GetMapping("{id}")
    @JsonView(Views.ShortNews.class)
    public Comment getOne(@PathVariable Long id) {
        return commentService.findById(id);
    }

    @PostMapping
    @JsonView(Views.ShortNews.class)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDto> create(@Valid @RequestBody Comment comment, BindingResult bindingResult) {
        AppUser principal = authorizationService.getPrincipal();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new CommentResponseDto(null, BindingResultUtils.getErrors(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                new CommentResponseDto(commentService.save(comment, principal), BindingResultUtils.getErrors(bindingResult)),
                HttpStatus.BAD_REQUEST);
    }
}
