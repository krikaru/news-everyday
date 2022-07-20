package com.example.newsapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.Cookie;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieUtilsTest {
    MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void findCookieByName_returnNullIfCookieByNameIsNotExist() {
        Cookie cookieByName = CookieUtils.findCookieByName(request, "any");
        assertThat(cookieByName).isNull();
    }

    @Test
    void findCookieByName_returnCookieIfCookieByNameIsExist() {
        Cookie cookie1 = new Cookie("name1", "val1");
        Cookie cookie2 = new Cookie("name2", "val2");
        Cookie cookie3 = new Cookie("name3", "val3");
        request.setCookies(cookie1, cookie2, cookie3);

        Cookie result = CookieUtils.findCookieByName(request, "name2");

        assertThat(Objects.requireNonNull(result).getValue()).isEqualTo("val2");
    }

    @Test
    void createHttpOnlyCookie_checkSetHttpOnlyForCookie() {
        String name = "name";
        String value = "value";

        Cookie result = CookieUtils.createHttpOnlyCookie(name, value);

        assertTrue(result.isHttpOnly());
    }
}