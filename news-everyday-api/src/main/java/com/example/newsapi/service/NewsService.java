package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.repo.NewsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepo newsRepo;

    public List<News> getAllNews() {
        return newsRepo.findAll();
    }

    public News save(AppUser author, News news) {
        news.setAuthor(author);
        return newsRepo.save(news);
    }
}
