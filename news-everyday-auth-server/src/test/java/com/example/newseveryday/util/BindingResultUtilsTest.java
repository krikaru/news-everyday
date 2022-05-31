package com.example.newseveryday.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BindingResultUtilsTest {
    BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        this.bindingResult = new BeanPropertyBindingResult("", "");
    }

    @Test
    void getMapErrorsIfBindingResultIsEmpty() {
        Map<String, List<String>> result = BindingResultUtils.getErrors(bindingResult);

        assertThat(result)
                .as("Result map couldn't contain entry")
                .isEmpty();
    }

    @Test
    void getMapErrorsFromBindingResult() {
        bindingResult.addError(new FieldError("objName1", "field1", "message11"));
        bindingResult.addError(new FieldError("objName1", "field1", "message12"));
        bindingResult.addError(new FieldError("objName1", "field2", "message21"));
        bindingResult.addError(new FieldError("objName1", "field2", "message22"));
        bindingResult.addError(new FieldError("objName1", "field2", "message23"));
        bindingResult.addError(new FieldError("objName1", "field3", "message31"));

        Map<String, List<String>> result = BindingResultUtils.getErrors(bindingResult);

        assertThat(result)
                .as("Result map doesn't contain added errors")
                .containsEntry("field1", List.of("message11", "message12"))
                .containsEntry("field2", List.of("message21", "message22", "message23"))
                .containsEntry("field3", List.of("message31"));
    }

    @Test
    void addErrorsToBindingResult() {
        String invalidFieldName = "some name";
        String errorMessage = "some message";

        BindingResultUtils.addErrors(bindingResult, invalidFieldName, errorMessage);

        assertThat(bindingResult.getFieldError().getField())
                .as("Error didn't add to BindingResult")
                .isEqualTo("some name");
        assertThat(bindingResult.getFieldError().getDefaultMessage())
                .as("Error didn't add to BindingResult")
                .isEqualTo("some message");

    }
}