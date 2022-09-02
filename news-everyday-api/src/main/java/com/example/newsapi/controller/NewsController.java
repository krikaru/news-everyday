package com.example.newsapi.controller;

import com.example.newsapi.dto.LikeDto;
import com.example.newsapi.dto.NewsErrorInfoDto;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.service.NewsService;
import com.example.newsapi.util.BindingResultUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("api/news")
@AllArgsConstructor
@NoArgsConstructor
public class NewsController {
    @Autowired
    private NewsService newsService;
    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<List<News>> getAllNews(@RequestParam(required = false) String sort) {
        return new ResponseEntity<>(newsService.getAllNews(sort), HttpStatus.OK);
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
    public ResponseEntity<NewsErrorInfoDto> create(@Valid @RequestBody News news,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(news, BindingResultUtils.getErrors(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        } else {
            AppUser principal = authorizationService.getPrincipal();
            News save = newsService.save(principal, news);
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(save, null),
                    HttpStatus.CREATED);
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('WRITER')")
    @JsonView(Views.ShortNews.class)
    public ResponseEntity<NewsErrorInfoDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody News news,
                                                   BindingResult bindingResult)
    {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new NewsErrorInfoDto(news, BindingResultUtils.getErrors(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<News> optionalNews = newsService.findById(id);
        AppUser principal = authorizationService.getPrincipal();
        if (optionalNews.isEmpty()) {
            BindingResultUtils.addErrors(bindingResult,
                    "id",
                    "Новость с таким id не существует!");
        } else {
            Long authorId = optionalNews.get().getAuthor().getId();
            if (!authorId.equals(principal.getId())) {
                BindingResultUtils.addErrors(bindingResult,
                        "author",
                        "Вы не можете изменить эту новость!");
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
        Map<String, List<String>> errors = new HashMap<>();

        if (optionalNews.isPresent()) {
            Long authorId = optionalNews.get().getAuthor().getId();
            if (authorId.equals(authorizationService.getPrincipal().getId())) {
                newsService.delete(id);
                return new ResponseEntity<>(null, HttpStatus.OK);
            } else {
                errors.put("author", List.of("Вы не можете удалить эту новость!"));
            }
        } else {
            errors.put("id", List.of("Новость с таким id не существует!"));
        }
        return new ResponseEntity<>(new NewsErrorInfoDto(null, errors), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("{id}/like")
    @JsonView(Views.ShortNews.class)
    @PreAuthorize("hasAnyAuthority('WRITER', 'USER')")
    public ResponseEntity<LikeDto> like(@PathVariable Long id) {
        Optional<News> optionalNews = newsService.findById(id);
        if (optionalNews.isPresent()) {
            int count = newsService.like(optionalNews.get(), authorizationService.getPrincipal());
            return new ResponseEntity<>(
                    new LikeDto(count, null),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    new LikeDto(-1, Map.of("like", List.of("Такой новости не существует"))),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
