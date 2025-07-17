package com.example.blog.service;

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

    public List<Comment> findByPostId(Integer postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteById(Integer id) {
        commentRepository.deleteById(id);
    }
}
