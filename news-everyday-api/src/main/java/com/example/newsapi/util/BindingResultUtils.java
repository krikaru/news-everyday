package com.example.newsapi.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BindingResultUtils {

    public static Map<String, List<String>> getErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())));
    }

    public static void addErrors(
            BindingResult bindingResult,
            String invalidFieldName,
            String errorMessage
    ) {
        bindingResult.addError(new FieldError("", invalidFieldName, errorMessage));
    }
}
