package com.example.newsapi.repo;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.LockAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LockRepo extends JpaRepository<LockAccount, Long> {

    boolean existsByBannedUser(AppUser bannedUser);

    Optional<LockAccount> getByBannedUserId(Long userId);
}
