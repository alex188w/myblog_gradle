package com.example.blog.service;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.CommentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final JdbcTemplate jdbcTemplate;

    public PostService(PostRepository postRepository,
            TagService tagService,
            CommentService commentService,
            CommentRepository commentRepository,
            JdbcTemplate jdbcTemplate) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.commentService = commentService;
        this.commentRepository = commentRepository;
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

    public void save(Post post) {
        save(post, Collections.emptyList());
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

    // для подсчета комментариев на странице posts по каждому посту
    public int getCommentCountByPostId(int postId) {
        return commentRepository.countByPostId(postId);
    }

    // Получение списка тегов по каждому посту
    public Map<Integer, List<Tag>> getTagsForPosts(List<Post> posts) {
        Map<Integer, List<Tag>> result = new HashMap<>();
        for (Post post : posts) {
            List<Tag> tags = tagService.findTagsByPostId(post.getId());
            result.put(post.getId(), tags);
        }
        return result;
    }

    // поиск по тегу
    public List<Post> findByTagName(String tagName) {
        String sql = """
                SELECT p.id, p.title, p.preview, p.image_url, p.text, p.likes
                FROM posts p
                JOIN post_tags pt ON p.id = pt.post_id
                JOIN tags t ON pt.tag_id = t.id
                WHERE t.name = ?
                ORDER BY p.id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setTitle(rs.getString("title"));
            post.setPreview(rs.getString("preview"));
            post.setImageUrl(rs.getString("image_url"));
            post.setText(rs.getString("text"));
            post.setLikes(rs.getInt("likes"));
            return post;
        }, tagName);
    }

    // для пагинации
    public List<Post> findPaginated(int page, int size, String tag) {
        int offset = page * size;
        if (tag != null && !tag.isBlank()) {
            String sql = """
                        SELECT p.* FROM posts p
                        JOIN post_tags pt ON p.id = pt.post_id
                        JOIN tags t ON t.id = pt.tag_id
                        WHERE t.name = ?
                        ORDER BY p.id DESC
                        LIMIT ? OFFSET ?
                    """;
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> mapRowToPost(rs),
                    tag, size, offset);
        } else {
            String sql = "SELECT * FROM posts ORDER BY id DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> mapRowToPost(rs),
                    size, offset);
        }
    }

    public int countPosts(String tag) {
        if (tag != null && !tag.isBlank()) {
            String sql = """
                        SELECT COUNT(*) FROM posts p
                        JOIN post_tags pt ON p.id = pt.post_id
                        JOIN tags t ON t.id = pt.tag_id
                        WHERE t.name = ?
                    """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tag);
            return (count != null) ? count : 0;
        } else {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
            return (count != null) ? count : 0;
        }
    }

    private Post mapRowToPost(ResultSet rs) throws SQLException {
        return new Post(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("preview"),
                rs.getString("image_url"),
                rs.getString("text"),
                rs.getInt("likes"));
    }
}
