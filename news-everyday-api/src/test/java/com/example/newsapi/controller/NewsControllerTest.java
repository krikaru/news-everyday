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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerTest {
    static final String NEWS_URL = "/api/news";
    static final String CHARACTER_ENCODING = "utf-8";

    static final String notValidRequestNewsHeader = "notV";
    static final String notValidRequestNewsText = "text";
    static final News notValidRequestNews = News.builder()
            .header(notValidRequestNewsHeader).text(notValidRequestNewsText).build();

    static final String validRequestNewsHeader = "valid_header";
    static final String validRequestNewsText = Strings.repeat("any", 34);
    static News validRequestNews = News.builder()
            .header(validRequestNewsHeader).text(validRequestNewsText).build();

    static final Long idFromDb = 1L;
    static final String headerFromDb = "Db_" + validRequestNewsHeader;
    static final String textFromDb = "Db_" + validRequestNewsText;
    static final AppUser author = AppUser.builder().id(3L).build();
    static final News newsFromDb = News.builder()
            .id(idFromDb).header(headerFromDb).text(textFromDb).author(author).build();

    static final News updatedNews = News.builder()
            .id(idFromDb).header(validRequestNewsHeader).text(validRequestNewsText).author(author).build();

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
        when(newsService.getAllNews(any(), any(), any())).thenReturn(newsList);

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
        Optional<News> optionalNews = Optional.of(newsFromDb);
        when(newsService.findById(any())).thenReturn(optionalNews);

        mockMvc.perform(get(NEWS_URL + "/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.header", equalTo(headerFromDb)))
                .andExpect(jsonPath("$.text", equalTo(textFromDb)));
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
        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post(NEWS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequestNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"USER"})
    void create_return405IfHasNotAuthority() {
        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post(NEWS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequestNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void create_returnBadRequestIfNewsFieldsAreNotValid() {
        mockMvc.perform(post(NEWS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null)))
                .andExpect(jsonPath("$.news.header", equalTo(notValidRequestNewsHeader)))
                .andExpect(jsonPath("$.news.text", equalTo(notValidRequestNewsText)))
                .andExpect(jsonPath("$.errors.header[0]", equalTo("Длина закголовка должна быть не меньше 5 и не больше 100 символов.")))
                .andExpect(jsonPath("$.errors.text[0]", equalTo("Длина основного текста должна быть не меньше 100 и не больше 10000 символов.")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "test@test.ru", authorities = {"WRITER"})
    void create_returnCreatedAndBodyIfNewsFieldsAreValid() {
        when(newsService.save(any(), any())).thenReturn(updatedNews);
        when(authorizationService.getPrincipal()).thenReturn(new AppUser());

        mockMvc.perform(post(NEWS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.news.id", equalTo(updatedNews.getId()), Long.class))
                .andExpect(jsonPath("$.news.header", equalTo(updatedNews.getHeader())))
                .andExpect(jsonPath("$.news.text", equalTo(updatedNews.getText())))
                .andExpect(jsonPath("$.errors", equalTo(null)));
    }

    @SneakyThrows
    @Test
    @WithAnonymousUser
    void update_return405IfAnonymousUser() {
        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(put(NEWS_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequestNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"USER"})
    void update_return405IfHasNotAuthority() {
        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(put(NEWS_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequestNews))
                    .characterEncoding(CHARACTER_ENCODING));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnBadRequestIfNewsFieldsAreNotValid() {
        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null)))
                .andExpect(jsonPath("$.news.header", equalTo(notValidRequestNewsHeader)))
                .andExpect(jsonPath("$.news.text", equalTo(notValidRequestNewsText)))
                .andExpect(jsonPath("$.errors.header[0]", equalTo("Длина закголовка должна быть не меньше 5 и не больше 100 символов.")))
                .andExpect(jsonPath("$.errors.text[0]", equalTo("Длина основного текста должна быть не меньше 100 и не больше 10000 символов.")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnBadRequestIfUpdatedNewsIsNotExistInDb() {
        when(newsService.findById(any())).thenReturn(Optional.empty());
        when(authorizationService.getPrincipal()).thenReturn(new AppUser());

        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null)))
                .andExpect(jsonPath("$.news.header", equalTo(validRequestNewsHeader)))
                .andExpect(jsonPath("$.news.text", equalTo(validRequestNewsText)))
                .andExpect(jsonPath("$.errors.id[0]", equalTo("Новость с таким id не существует!")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnBadRequestIfPrincipalNotEqualsToAuthorOfNews() {
        when(newsService.findById(any())).thenReturn(Optional.of(newsFromDb));
        when(authorizationService.getPrincipal()).thenReturn(AppUser.builder().id(2L).build());

        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.news.id", equalTo(null), Long.class))
                .andExpect(jsonPath("$.news.header", equalTo(validRequestNewsHeader)))
                .andExpect(jsonPath("$.news.text", equalTo(validRequestNewsText)))
                .andExpect(jsonPath("$.errors.author[0]", equalTo("Вы не можете изменить эту новость!")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void update_returnOkIfNewsValidAndUserHasPermission() {
        when(newsService.findById(any())).thenReturn(Optional.of(newsFromDb));
        when(authorizationService.getPrincipal()).thenReturn(AppUser.builder().id(3L).build());
        when(newsService.update(any(), any())).thenReturn(updatedNews);

        mockMvc.perform(put(NEWS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestNews))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news.id", equalTo(updatedNews.getId()), Long.class))
                .andExpect(jsonPath("$.news.header", equalTo(updatedNews.getHeader())))
                .andExpect(jsonPath("$.news.text", equalTo(updatedNews.getText())))
                .andExpect(jsonPath("$.errors", equalTo(null)));
    }

    @SneakyThrows
    @Test
    @WithAnonymousUser
    void delete_return405IfAnonymousUser() {
        NestedServletException exception = Assertions.assertThrows(
                NestedServletException.class, () -> mockMvc.perform(delete(NEWS_URL + "/1")));

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"USER"})
    void delete_return405IfHasNotAuthority() {
        NestedServletException exception = Assertions.assertThrows(
                NestedServletException.class, () -> mockMvc.perform(delete(NEWS_URL + "/1")));

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void delete_returnBadRequestIfDeletedNewsIsNotExistInDb() {
        when(newsService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(delete(NEWS_URL + "/1"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id[0]", equalTo("Новость с таким id не существует!")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void delete_returnBadRequestIfPrincipalNotEqualsToAuthorOfNews() {
        when(newsService.findById(any())).thenReturn(Optional.of(newsFromDb));
        when(authorizationService.getPrincipal()).thenReturn(AppUser.builder().id(2L).build());

        mockMvc.perform(delete(NEWS_URL + "/1"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.author[0]", equalTo("Вы не можете удалить эту новость!")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void delete_returnOkIfNewsExistsAndUserHasPermission() {
        when(newsService.findById(any())).thenReturn(Optional.of(newsFromDb));
        when(authorizationService.getPrincipal()).thenReturn(AppUser.builder().id(3L).build());
        doNothing().when(newsService).delete(any());
        mockMvc.perform(delete(NEWS_URL + "/1"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @SneakyThrows
    @Test
    @WithAnonymousUser
    void like_return405IfAnonymousUser() {
        NestedServletException exception = Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get(NEWS_URL + "/1/like"));
        });

        assertThat(exception.getRootCause().getMessage()).isEqualTo("Access is denied");
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void like_return400IfNewsNotExists() {
        when(newsService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(NEWS_URL + "/1/like"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.like[0]", equalTo("Такой новости не существует")));
    }

    @SneakyThrows
    @Test
    @WithMockUser(authorities = {"WRITER"})
    void like_returnOkIfNewsExists() {
        int count = 1;
        when(newsService.findById(any())).thenReturn(Optional.of(newsFromDb));
        when(newsService.like(any(), any())).thenReturn(count);
        when(authorizationService.getPrincipal()).thenReturn(new AppUser());

        mockMvc.perform(get(NEWS_URL + "/1/like"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(count)))
                .andExpect(jsonPath("$.errors", equalTo(null)));
    }

}