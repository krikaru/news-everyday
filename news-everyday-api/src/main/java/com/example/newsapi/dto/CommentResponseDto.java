package com.example.newsapi.dto;

import com.example.newsapi.model.Comment;
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
public class CommentResponseDto {
    @JsonView(Views.ShortNews.class)
    private Comment comment;
    @JsonView(Views.ShortNews.class)
    private Map<String, List<String>> errors;
}
