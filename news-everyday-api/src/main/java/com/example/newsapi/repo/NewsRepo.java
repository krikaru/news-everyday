package com.example.newsapi.repo;

import com.example.newsapi.model.News;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepo extends JpaRepository<News, Long> {

    @EntityGraph(attributePaths = { "comments" })
    @Query("from News n " +
            "left join n.comments c " +
            "where (cast(:filterDate as date) is null or cast(:filterDate as date) = cast(n.creationDate as date)) AND " +
            "(:author is null or :author = n.author.id ) " +
            "group by n.id, n.creationDate, c.id " +
            "order by size(n.likes) desc, n.creationDate desc")
    List<News> findAllSortByQuantityLike(@Param("filterDate") String date, @Param("author") Long author);

    @EntityGraph(attributePaths = { "comments" })
    @Query("from News n " +
            "left join n.comments c " +
            "where (cast(:filterDate as date) is null or cast(:filterDate as date) = cast(n.creationDate as date)) AND " +
            "(:author is null or :author = n.author.id ) " +
            "group by n.id, n.creationDate, c.id " +
            "order by size(n.comments) desc, n.creationDate desc")
    List<News> findAllSortByQuantityComment(@Param("filterDate") String date, @Param("author") Long author);

    @EntityGraph(attributePaths = { "comments" })
    @Query("from News n " +
            "left join n.comments c " +
            "where (cast(:filterDate as date) is null or cast(:filterDate as date) = cast(n.creationDate as date)) AND " +
            "(:author is null or :author = n.author.id ) " +
            "group by n.id, n.creationDate, c.id " +
            "order by n.creationDate desc")
    List<News> findAllSortByCreationDate(@Param("filterDate") String date, @Param("author") Long author);
}
