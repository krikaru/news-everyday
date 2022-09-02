package com.example.newsapi.repo;

import com.example.newsapi.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepo extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n ORDER BY size(n.likes) DESC, n.creationDate ASC")
    List<News> findAllSortByQuantityLike();

    @Query("SELECT n FROM News n ORDER BY n.creationDate ASC")
    List<News> findAllSortByCreationDate();
}
