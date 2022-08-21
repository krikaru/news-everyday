package com.example.newsapi.controller;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.News;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.NestedServletException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerTest {
    static final String NEWS_URL = "/api/news";
    static final String CHARACTER_ENCODING = "utf-8";

    @MockBean
    NewsService newsService;
    @MockBean
    AuthorizationService authorizationService;

    NewsController newsController;
    BindingResult bindingResult;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        newsController = spy(new NewsController());
        bindingResult = new BeanPropertyBindingResult(new Object(), "any");
        objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    @Test
    void getAllNews_ifNewsIsExist() {
        List<News> newsList = List.of(News.builder().header("header1").text("text1").build(),
                News.builder().header("header1").text("text1").build());
        when(newsService.getAllNews()).thenReturn(newsList);

        mockMvc.perform(get(NEWS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @SneakyThrows
    @Test
    void getAllNews_ifNewsIsNotExist() {
        mockMvc.perform(get(NEWS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @SneakyThrows
    @Test
    void getOneNews_ifNewsIsExist() {
        String header = "header";
        String text = "text";
        News news = News.builder().id(1L).header(header).text(text).build();
        Optional<News> optionalNews = Optional.of(news);
        when(newsService.findById(any())).thenReturn(optionalNews);

        mockMvc.perform(get(NEWS_URL + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.header", equalTo(header)))
                .andExpect(jsonPath("$.text", equalTo(text)));
    }

    @SneakyThrows
    @Test
    void getOneNews_ifNewsIsNotExist() {
        when(newsService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(NEWS_URL + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @SneakyThrows
    @Test
    @WithAnonymousUser
    void create_return405IfAnonymousUser() {
        News requestBodyNews = News.builder().header("any").text("any").build();

        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post(NEWS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBodyNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"USER"})
    void create_return405IfHasNotAuthority() {
        News requestBodyNews = News.builder().header("any").text("any").build();

        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post(NEWS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBodyNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void create_returnBadRequestIfNewsFieldsAreNotValid() {
        News requestBodyNews = News.builder().header("NV").text("Not_Valid_text_less_ten_100").build();
        mockMvc.perform(post(NEWS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null)))
                .andExpect(jsonPath("$.news.header", equalTo("NV")))
                .andExpect(jsonPath("$.news.text", equalTo("Not_Valid_text_less_ten_100")))
                .andExpect(jsonPath("$.errors.text[0]", equalTo("Длина основного текста должна быть не меньше 100 и не больше 10000 символов.")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "test@test.ru", authorities = {"WRITER"})
    void create_returnCreatedAndBodyIfNewsFieldsAreValid() {
        Long id = 1L;
        String text = Strings.repeat("any", 34);
        String header = "valid_header";
        News requestBodyNews = News.builder().header(header).text(text).build();
        News newsFromDb = News.builder().id(id).header(header).text(text).build();
        when(newsService.save(any(), any())).thenReturn(newsFromDb);
        when(authorizationService.getPrincipal()).thenReturn(new AppUser());

        mockMvc.perform(post(NEWS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.news.id", equalTo(id), Long.class))
                .andExpect(jsonPath("$.news.header", equalTo(header)))
                .andExpect(jsonPath("$.news.text", equalTo(text)))
                .andExpect(jsonPath("$.errors", equalTo(null)));
    }

    @SneakyThrows
    @Test
    @WithAnonymousUser
    void update_return405IfAnonymousUser() {
        News requestBodyNews = News.builder().header("any").text("any").build();

        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(put(NEWS_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBodyNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"USER"})
    void update_return405IfHasNotAuthority() {
        News requestBodyNews = News.builder().header("any").text("any").build();

        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(put(NEWS_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBodyNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnBadRequestIfNewsFieldsAreNotValid() {
        News requestBodyNews = News.builder().header("NV").text("Not_Valid_text_less_ten_100").build();
        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null)))
                .andExpect(jsonPath("$.news.header", equalTo("NV")))
                .andExpect(jsonPath("$.news.text", equalTo("Not_Valid_text_less_ten_100")))
                .andExpect(jsonPath("$.errors.text[0]", equalTo("Длина основного текста должна быть не меньше 100 и не больше 10000 символов.")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnBadRequestIfUpdatedNewsIsNotExistInDb() {
        Long id = 1L;
        String text = Strings.repeat("any", 34);
        String header = "valid_header";
        News requestBodyNews = News.builder().id(id).header(header).text(text).build();
        when(newsService.findById(any())).thenReturn(Optional.empty());
        when(authorizationService.getPrincipal()).thenReturn(new AppUser());

        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(id), Long.class))
                .andExpect(jsonPath("$.news.header", equalTo(header)))
                .andExpect(jsonPath("$.news.text", equalTo(text)))
                .andExpect(jsonPath("$.errors.id[0]", equalTo("Новость с таким id не существует!")));
    }

    @Test
    void delete() {
    }
}