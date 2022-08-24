package com.example.newsapi.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(of = {"id", "header", "text"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {
    @Id
    @SequenceGenerator(name = "news_id_sequence", sequenceName = "news_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id_sequence")
    @JsonView(Views.ShortNews.class)
    private Long id;

    @JsonView(Views.ShortNews.class)
    @NotBlank(message = "У новости должен быть заголовок!")
    @Length(message = "Длина закголовка должна быть не меньше 5 и не больше 100 символов.", max=100, min = 5)
    private String header;

    @JsonView(Views.ShortNews.class)
    @NotBlank(message = "У новости должен быть основной текст!")
    @Length(message = "Длина основного текста должна быть не меньше 100 и не больше 10000 символов.", max=10000, min = 100)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonView(Views.ShortNews.class)
    private AppUser author;

    @ManyToMany
    @JoinTable(
            name = "news_likes",
            joinColumns = {@JoinColumn(name = "news_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<AppUser> likes;
}
