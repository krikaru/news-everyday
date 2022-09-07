package com.example.newsapi.controller;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Comment;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.service.CommentService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Comment create(@RequestBody Comment comment) {
        AppUser principal = authorizationService.getPrincipal();
        return commentService.save(comment, principal);
    }
}
