package com.example.newsapi.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN, WRITER;

    @Override
    public String getAuthority() {
        return name();
    }
}
