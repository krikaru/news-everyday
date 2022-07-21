package com.example.newsapi.controller;

import com.example.newsapi.model.News;
import com.example.newsapi.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('USER')")
    public News create(@RequestBody News news) {
        return newsService.save(news);
    }
}
