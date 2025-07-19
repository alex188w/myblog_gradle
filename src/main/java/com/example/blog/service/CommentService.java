package com.example.blog.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.blog.model.Comment;
import com.example.blog.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void addComment(Integer postId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor("anon"); // или получить из SecurityContext, если есть авторизация
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void updateComment(Integer commentId, String content) {
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        if (existing != null) {
            existing.setContent(content);
            commentRepository.save(existing);
        }
    }

    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Comment> findByPostId(Integer postId) {
        return commentRepository.findByPostId(postId);
    }
}
