package com.example.blog.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.blog.model.Post;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Integer> {
    List<Post> findByTitleContainingIgnoreCase(String titlePart);
}
