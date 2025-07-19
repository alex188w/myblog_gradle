package com.example.blog.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.blog.model.Comment;

public interface CommentRepository extends CrudRepository<Comment, Integer> {
    List<Comment> findByPostId(Integer postId);
    int countByPostId(int postId);
}
