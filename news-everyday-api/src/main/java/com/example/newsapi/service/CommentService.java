package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.Comment;
import com.example.newsapi.repo.CommentRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class CommentService {
    @Autowired
    private CommentRepo commentRepo;

    public Comment findById(Long id) {
        return commentRepo.getById(id);
    }

    public List<Comment> findAll() {
        return commentRepo.findAll();
    }

    public Comment save(Comment comment, AppUser author) {
        comment.setCreationDate(LocalDateTime.now());
        comment.setAuthor(author);
        return commentRepo.save(comment);
    }
}
