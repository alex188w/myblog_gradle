package com.example.blog.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.blog.model.Tag;

public interface TagRepository extends CrudRepository<Tag, Integer> {
    Tag findByName(String name);
}
