package com.example.newsapi.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class News {
    @Id
    @SequenceGenerator(name = "news_id_sequence", sequenceName = "news_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id_sequence")
    @JsonView(Views.ShortNews.class)
    private Long id;

    @JsonView(Views.ShortNews.class)
    private String header;
    @JsonView(Views.ShortNews.class)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonView(Views.ShortNews.class)
    private AppUser author;
}
