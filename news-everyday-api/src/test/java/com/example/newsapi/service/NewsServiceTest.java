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
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.stream.Stream;

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

    @MockBean
    NewsRepo newsRepo;
    @InjectMocks
    NewsService newsService;

    @Captor
    ArgumentCaptor<String> capturedString;
    @Captor
    ArgumentCaptor<Integer> capturedInteger;

    @BeforeEach
    void setUp() {
        likes.add(user2);
        likes.add(user3);
    }
    @AfterEach
    void tearDown() {
        news.setLikes(null);
    }

    private static Stream<Arguments> provideArgumentsForGetAllNews() {
        return Stream.of(
                Arguments.of(null, null, null, -1, "-1"),
                Arguments.of("unknown", null, null, -1, "-1"),
                Arguments.of("date", null, null, -1, "-1"),
                Arguments.of("date", 1, null, 1, "-1"),
                Arguments.of("date", null, "2022-08-25", -1, "2022-08-25"),
                Arguments.of("date", null, "2022-25-25", -1, "-1"),
                Arguments.of("date", 1, "2022-08-25", 1, "2022-08-25"),
                Arguments.of("like", null, null, -1, "-1"),
                Arguments.of("like", 1, null, 1, "-1"),
                Arguments.of("like", null, "2022-08-25", -1, "2022-08-25"),
                Arguments.of("like", null, "2022-25-25", -1, "-1"),
                Arguments.of("like", 1, "2022-08-25", 1, "2022-08-25")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForGetAllNews")
    void getAllNews_sortWithArguments(String sort, Integer authorId, String date,
                                                     Integer expectedAuthorId, String expectedDate) {
        when(newsRepo.findAllSortByCreationDate(date, authorId)).thenReturn(null);
        when(newsRepo.findAllSortByQuantityLike(date, authorId)).thenReturn(null);

        newsService.getAllNews(sort, authorId, date);

        if (sort == null) {
            verify(newsRepo).findAllSortByCreationDate(capturedString.capture(), capturedInteger.capture());
        } else {
            switch (sort) {
                case "like" -> verify(newsRepo).findAllSortByQuantityLike(capturedString.capture(), capturedInteger.capture());
                default -> verify(newsRepo).findAllSortByCreationDate(capturedString.capture(), capturedInteger.capture());
            }
        }
        assertThat(capturedInteger.getValue()).isEqualTo(expectedAuthorId);
        assertThat(capturedString.getValue()).isEqualTo(expectedDate);
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