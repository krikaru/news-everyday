package com.example.newseveryday.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokensResponseDto {
    private String access_token;
    private String refresh_token;
}
