package com.example.newsapi.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usr")
@Data
@ToString(exclude = {"roles", "newsList", "comments"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"email"})
public class AppUser {
    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence"
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    @JsonView(Views.ShotUser.class)
    private Long id;

    @JsonView(Views.ShotUser.class)
    private String firstName;

    @JsonView(Views.ShotUser.class)
    private String email;

    @JsonView(Views.FullUser.class)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "author")
    @JsonView(Views.FullUser.class)
    private List<News> newsList = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    @JsonView(Views.FullUser.class)
    private List<Comment> comments;

    private static final long serialVersionUID = -7769347416115841247L;
}
