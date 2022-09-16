package com.example.newsapi.repo;

import com.example.newsapi.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = { "roles" })
    Optional<AppUser> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = { "roles" })
    Optional<AppUser> findById(Long id);
}

