package com.example.newsapi.controller;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.NewsService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public List<News> getAllNews() {
        return newsService.getAllNews();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITER')")
    @JsonView(Views.ShortNews.class)
    public News create(Authentication author, @RequestBody News news) {
        AppUser principal = (AppUser) author.getPrincipal();
        return newsService.save(principal, news);
    }
}
