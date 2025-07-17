package com.example.blog.service;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import com.example.blog.repository.PostRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final CommentService commentService;
    private final JdbcTemplate jdbcTemplate;

    public PostService(PostRepository postRepository,
                       TagService tagService,
                       CommentService commentService,
                       JdbcTemplate jdbcTemplate) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.commentService = commentService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> findAll() {
        return (List<Post>) postRepository.findAll();
    }

    public List<Post> findByTitle(String titlePart) {
        return postRepository.findByTitleContainingIgnoreCase(titlePart);
    }

    public Post findById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post save(Post post, List<String> tagNames) {
        // Сохраняем пост
        Post savedPost = postRepository.save(post);

        // Обновляем теги — удаляем старые связи, добавляем новые
        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", savedPost.getId());

        for (String tagName : tagNames) {
            Tag tag = tagService.save(new Tag(null, tagName.trim()));

            jdbcTemplate.update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)",
                    savedPost.getId(), tag.getId());
        }

        return savedPost;
    }

    public void delete(Integer id) {
        postRepository.deleteById(id);
    }

    public List<Tag> findTagsByPostId(Integer postId) {
        return tagService.findTagsByPostId(postId);
    }

    public List<Comment> findCommentsByPostId(Integer postId) {
        return commentService.findByPostId(postId);
    }
}
