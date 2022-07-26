package com.example.newsapi.controller;

import com.example.newsapi.dto.NewsErrorInfoDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.NewsService;
import com.example.newsapi.util.BindingResultUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<List<News>> getAllNews() {
        List<News> newsList = newsService.getAllNews();
        return newsList.isEmpty() ?
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(newsList, HttpStatus.OK);
    }

    @GetMapping("{id}")
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<News> getOneNews(@PathVariable Long id) {
        Optional<News> optionalNews = newsService.findById(id);
        return optionalNews.isPresent() ?
                new ResponseEntity<>(optionalNews.get(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITER')")
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<NewsErrorInfoDto> create(Authentication author,
                                                   @Valid @RequestBody News news,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(news, BindingResultUtils.getErrors(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        } else {
            AppUser principal = (AppUser) author.getPrincipal();
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(newsService.save(principal, news), null),
                    HttpStatus.OK);
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('WRITER')")
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<NewsErrorInfoDto> update(Authentication author,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody News news,
                                                   BindingResult bindingResult)
    {
        Optional<News> optionalNews = newsService.findById(id);
        AppUser principal = (AppUser) author.getPrincipal();
        if (optionalNews.isEmpty()) {
            BindingResultUtils.addErrors(bindingResult,
                    "id",
                    "Новость с таким id не существует!");
        } else {
            Long authorId = optionalNews.get().getAuthor().getId();
            if (!authorId.equals(principal.getId())) {
                BindingResultUtils.addErrors(bindingResult,
                        "author",
                        "Вы не можете удалить эту новость!");
            }
        }

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(news, BindingResultUtils.getErrors(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(newsService.update(news, optionalNews.get()), null),
                    HttpStatus.OK);
        }


    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('WRITER')")
    public ResponseEntity<NewsErrorInfoDto> delete(@PathVariable Long id) {
        Optional<News> optionalNews = newsService.findById(id);
        Map<String, List<String>> errors;
        if (optionalNews.isPresent()) {
            newsService.delete(id);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        errors = Map.of("id", List.of("Новость с таким id не существует!"));
        return new ResponseEntity<>(new NewsErrorInfoDto(optionalNews.get(), errors), HttpStatus.BAD_REQUEST);
    }

}
