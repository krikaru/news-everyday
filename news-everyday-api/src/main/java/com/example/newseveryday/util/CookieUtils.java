package com.example.newseveryday.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class CookieUtils {
    public static Cookie findCookieByName(HttpServletRequest request, String name) {
        return request.getCookies() == null ? null : Arrays.stream(
                request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst().orElse(null);
    }
}
