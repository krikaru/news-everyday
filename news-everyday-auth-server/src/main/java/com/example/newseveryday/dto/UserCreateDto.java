package com.example.newseveryday.dto;

import com.example.newseveryday.model.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Пароль должен быть заполнен.")
    @Length(min=6, max=15, message = "Длина пароля должна быть не меньше 6 и не больше 15 символов.")
    @Pattern(regexp = "^\\S+$")
    private String password;

    @NotBlank(message = "Пароль должен быть заполнен.")
    @Length(min=6, max=15, message = "Длина пароля должна быть не меньше 6 и не больше 15 символов.")
    @Pattern(regexp = "^\\S+$")
    private String confirmPassword;

    @NotBlank(message = "Имя не должно быть пустым.")
    @Length(message = "Длина имени должна быть не меньше 3 и не больше 25 символов.", max=30, min = 3)
    @Pattern(message = "Имя может содержать только буквы русского и английского алфавита.",
            regexp = "^[a-zA-Zа-яА-ЯёЁ -]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    @JsonView(Views.ShortInfo.class)
    private String firstName;

    @NotBlank(message = "Email должен быть заполнен.")
    @Email(message = "Неверный формат email.")
    @JsonView(Views.ShortInfo.class)
    private String email;

    @JsonView(Views.ShortInfo.class)
    private Map<String, List<String>> errors;
}
