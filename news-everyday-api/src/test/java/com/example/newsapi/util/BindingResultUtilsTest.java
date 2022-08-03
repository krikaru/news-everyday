package com.example.newsapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BindingResultUtilsTest {
    BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        bindingResult = new BeanPropertyBindingResult(new Object(), "any");
    }

    @Test
    void getErrors_checkReturnedMapIfNotEmpty() {
        bindingResult.addError(new FieldError("", "password", "wrong password format"));
        bindingResult.addError(new FieldError("", "name", "should be 2-10 symbols"));
        bindingResult.addError(new FieldError("", "name", "can't be empty"));

        Map<String, List<String>> result = BindingResultUtils.getErrors(bindingResult);

        assertThat(result.get("password")).contains("wrong password format");
        assertThat(result.get("name")).contains("should be 2-10 symbols", "can't be empty");
        assertThat(result.get("name")).contains("can't be empty");
    }

    @Test
    void getErrors_checkReturnedMapIfEmpty() {
        Map<String, List<String>> result = BindingResultUtils.getErrors(bindingResult);
        assertThat(result).isEmpty();
    }

    @Test
    void addErrors_checkAddingToBindingResult() {
        BindingResultUtils.addErrors(bindingResult, "password", "wrong password format");
        BindingResultUtils.addErrors(bindingResult, "name", "should be 2-10 symbols");
        BindingResultUtils.addErrors(bindingResult, "name", "can't be empty");

        assertThat(bindingResult.getFieldError("password").getDefaultMessage()).isEqualTo("wrong password format");
        assertThat(bindingResult.getFieldErrors("name").get(0).getDefaultMessage()).isEqualTo("should be 2-10 symbols");
        assertThat(bindingResult.getFieldErrors("name").get(1).getDefaultMessage()).isEqualTo("can't be empty");
        }
}