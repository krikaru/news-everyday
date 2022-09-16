package com.example.newsapi.dto;

import com.example.newsapi.model.AppUser;
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
public class UserResponseDto {
    @JsonView(Views.ShotUser.class)
    private AppUser user;
    @JsonView(Views.ShotUser.class)
    private Map<String, List<String>> errors;
}
