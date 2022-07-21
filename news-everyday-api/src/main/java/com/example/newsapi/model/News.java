package com.example.newsapi.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class News {
    @Id
    @SequenceGenerator(name = "news_id_sequence", sequenceName = "news_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id_sequence")
    private Long id;

    private String header;
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;
}
