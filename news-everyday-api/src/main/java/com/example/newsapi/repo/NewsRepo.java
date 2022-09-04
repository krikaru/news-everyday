package com.example.newsapi.repo;

import com.example.newsapi.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepo extends JpaRepository<News, Long> {

//    :filterDate='-1' потому что :filterDate IS NULL не сработает!!!!!!! Подробнее читать здесь:
//    https://groups.google.com/g/pgsql.interfaces.jdbc/c/oaU0fkyS4p8?pli=1
    @Query(value = "SELECT id, header, text, author_id, creation_date FROM news AS n " +
            "LEFT JOIN news_likes AS l on n.id = l.news_id " +
            "WHERE (:filterDate='-1' OR to_char(n.creation_date, 'yyyy-mm-dd') = :filterDate) AND " +
            "(:author=-1 OR n.author_id = :author) " +
            "GROUP BY n.id, n.creation_date " +
            "ORDER BY count(l.news_id) DESC, n.creation_date;"
            , nativeQuery = true)
    List<News> findAllSortByQuantityLike(@Param("filterDate") String date, @Param("author") Integer author);

    @Query(value = "SELECT id, header, text, author_id, creation_date FROM news AS n " +
            "LEFT JOIN news_likes AS l on n.id = l.news_id " +
            "WHERE (:filterDate='-1' OR to_char(n.creation_date, 'yyyy-mm-dd') = :filterDate) AND " +
            "(:author=-1 OR n.author_id = :author) " +
            "GROUP BY n.id, n.creation_date " +
            "ORDER BY n.creation_date;"
            , nativeQuery = true)
    List<News> findAllSortByCreationDate(@Param("filterDate") String date, @Param("author") Integer author);
}
