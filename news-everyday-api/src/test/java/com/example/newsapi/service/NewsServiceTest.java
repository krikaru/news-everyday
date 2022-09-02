package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.repo.NewsRepo;
import com.google.common.base.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class NewsServiceTest {
    static final AppUser principal = AppUser.builder().id(1L).email("email1").build();
    static final AppUser user2 = AppUser.builder().id(2L).email("email2").build();
    static final AppUser user3 = AppUser.builder().id(3L).email("email3").build();
    static final String header = "valid_header";
    static final String text = Strings.repeat("any", 34);
    static final News news = News.builder()
            .header(header).text(text).build();
    static final HashSet<AppUser> likes = new HashSet<>();

    @BeforeEach
    void setUp() {
        likes.add(user2);
        likes.add(user3);
    }
    @AfterEach
    void tearDown() {
        news.setLikes(null);
    }

    @MockBean
    NewsRepo newsRepo;

    @InjectMocks
    NewsService newsService;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"unknown"})
    void getAllNews_sortByDateIfParamIsNullOrUnknown(String sort) {
        when(newsRepo.findAllSortByCreationDate()).thenReturn(null);
        when(newsRepo.findAllSortByQuantityLike()).thenReturn(null);

        newsService.getAllNews(sort);

        verify(newsRepo).findAllSortByCreationDate();
    }

    @Test
    void getAllNews_sortByLikeIfParamEqualsLike() {
        when(newsRepo.findAllSortByCreationDate()).thenReturn(null);
        when(newsRepo.findAllSortByQuantityLike()).thenReturn(null);

        newsService.getAllNews("like");

        verify(newsRepo).findAllSortByQuantityLike();
    }

    @Test
    void like_ifPrincipalLikeNews() {
        news.setLikes(likes);
        int count = likes.size();
        when(newsRepo.save(any())).thenReturn(news);

        int result = newsService.like(news, principal);

        assertThat(result).isEqualTo(count + 1);
    }

    @Test
    void like_ifPrincipalDislikeNews() {
        likes.add(principal);
        news.setLikes(likes);
        int count = likes.size();
        when(newsRepo.save(any())).thenReturn(news);

        int result = newsService.like(news, principal);

        assertThat(result).isEqualTo(count - 1);
    }
}