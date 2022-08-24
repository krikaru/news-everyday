package com.example.newsapi.dto;

import com.example.newsapi.model.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class LikeDto {
    @JsonView(Views.ShortNews.class)
    private int count;
    @JsonView(Views.ShortNews.class)
    private Map<String, List<String>> errors;
}
