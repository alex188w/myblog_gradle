package com.example.blog.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.blog.model.Tag;
import com.example.blog.repository.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;

    public TagService(TagRepository tagRepository, JdbcTemplate jdbcTemplate) {
        this.tagRepository = tagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tag> findTagsByPostId(Integer postId) {
        String sql = "SELECT t.id, t.name FROM tags t " +
                     "JOIN post_tags pt ON t.id = pt.tag_id " +
                     "WHERE pt.post_id = ?";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Tag(rs.getInt("id"), rs.getString("name")),
                postId);
    }

    public Tag save(Tag tag) {
        Tag existing = tagRepository.findByName(tag.getName());
        if (existing != null) {
            return existing;
        }
        return tagRepository.save(tag);
    }
}
