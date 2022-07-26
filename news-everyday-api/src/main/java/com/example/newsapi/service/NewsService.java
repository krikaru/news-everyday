package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.repo.NewsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public void delete(Long id) {
        newsRepo.deleteById(id);
    }

    public Optional<News> findById(Long id) {
        return newsRepo.findById(id);
    }

    public News update(News newNews, News newsFromDb) {
        newsFromDb.setHeader(newNews.getHeader());
        newsFromDb.setText(newNews.getText());
        return newsRepo.save(newsFromDb);
    }
}
