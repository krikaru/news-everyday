package com.example.newsapi.dto;

import com.example.newsapi.model.News;
import com.example.newsapi.model.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsErrorInfoDto {
    @JsonView(Views.ShortNews.class)
    private News news;
    @JsonView(Views.ShortNews.class)
    private Map<String, List<String>> errors;
}
