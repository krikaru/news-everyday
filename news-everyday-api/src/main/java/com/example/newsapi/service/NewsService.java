package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.repo.NewsRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class NewsService {
    @Autowired
    private NewsRepo newsRepo;

    public List<News> getAllNews(String sort) {
        return sort == null ? newsRepo.findAllSortByCreationDate() : switch (sort) {
            case "like" -> newsRepo.findAllSortByQuantityLike();
            case "comment" -> throw new UnsupportedOperationException("It's not realised method yet");
            default -> newsRepo.findAllSortByCreationDate();
        };
    }

    public News save(AppUser author, News news) {
        news.setCreationDate(LocalDateTime.now());
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

    public int like(News news, AppUser principal) {
        Set<AppUser> likes = news.getLikes();
        if (likes.contains(principal)) {
            likes.remove(principal);
        } else {
            likes.add(principal);
        }
        newsRepo.save(news);
        return likes.size();
    }
}
