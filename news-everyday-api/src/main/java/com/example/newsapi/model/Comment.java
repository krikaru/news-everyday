package com.example.newsapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_id_sequence")
    @SequenceGenerator(name = "comments_id_sequence", sequenceName = "comments_id_sequence", allocationSize = 1)
    @JsonView(Views.ShortNews.class)
    private Long id;

    @NotBlank(message = "Комент не должен быть пустым!")
    @Length(message = "Длина комента должна быть не меньше 5 и не больше 10000 символов.", max=10000, min = 5)
    @JsonView(Views.ShortNews.class)
    private String text;

    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    @JsonView(Views.ShortNews.class)
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "news_id")
    @JsonView(Views.FullNews.class)
    private News news;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    @JsonView(Views.ShortNews.class)
    private AppUser author;
}
