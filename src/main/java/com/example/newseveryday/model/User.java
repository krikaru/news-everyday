package com.example.newseveryday.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "usr")
@Data
public class User {
    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence"
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    @JsonView(Views.ShotUser.class)
    private Long id;

    @JsonView(Views.ShotUser.class)
    private String username;

    private String password;

    @JsonView(Views.ShotUser.class)
    private String firstName;

    @JsonView(Views.ShotUser.class)
    private String email;
}
