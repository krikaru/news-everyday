package com.example.newsapi.dto;

import com.example.newsapi.model.LockAccount;
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
public class LockAccountResponseDto {
    @JsonView(Views.FullLockAccount.class)
    private LockAccount lockAccount;
    @JsonView(Views.FullLockAccount.class)
    private Map<String, List<String>> errors;
}
