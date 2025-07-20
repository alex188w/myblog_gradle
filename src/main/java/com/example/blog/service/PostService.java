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

/**
 * Сервис для работы с постами.
 * Отвечает за операции создания, обновления, удаления, поиска и пагинации
 * постов,
 * а также управление связями с тегами и комментариями.
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param postRepository    репозиторий для работы с постами
     * @param tagService        сервис для работы с тегами
     * @param commentService    сервис для работы с комментариями
     * @param commentRepository репозиторий для работы с комментариями
     * @param jdbcTemplate      JdbcTemplate для выполнения SQL-запросов
     */
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

    /**
     * Возвращает список всех постов.
     *
     * @return список всех постов
     */
    public List<Post> findAll() {
        return (List<Post>) postRepository.findAll();
    }

    /**
     * Находит посты, заголовок которых содержит заданную строку (без учета
     * регистра).
     *
     * @param titlePart часть заголовка для поиска
     * @return список подходящих постов
     */
    public List<Post> findByTitle(String titlePart) {
        return postRepository.findByTitleContainingIgnoreCase(titlePart);
    }

    /**
     * Находит пост по его идентификатору.
     *
     * @param id идентификатор поста
     * @return найденный пост или null, если пост не найден
     */
    public Post findById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    /**
     * Сохраняет пост и обновляет связи с тегами.
     * Удаляет старые связи с тегами и добавляет новые.
     *
     * @param post     пост для сохранения
     * @param tagNames список названий тегов
     * @return сохранённый пост с обновлённым состоянием
     */
    public Post save(Post post, List<String> tagNames) {
        Post savedPost = postRepository.save(post);

        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", savedPost.getId());

        for (String tagName : tagNames) {
            Tag tag = tagService.save(new Tag(null, tagName.trim()));

            jdbcTemplate.update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)",
                    savedPost.getId(), tag.getId());
        }

        return savedPost;
    }

    /**
     * Сохраняет пост без тегов.
     *
     * @param post пост для сохранения
     */
    public void save(Post post) {
        save(post, Collections.emptyList());
    }

    /**
     * Удаляет пост по идентификатору.
     *
     * @param id идентификатор поста для удаления
     */
    public void delete(Integer id) {
        postRepository.deleteById(id);
    }

    /**
     * Получает список тегов, связанных с указанным постом.
     *
     * @param postId идентификатор поста
     * @return список тегов поста
     */
    public List<Tag> findTagsByPostId(Integer postId) {
        return tagService.findTagsByPostId(postId);
    }

    /**
     * Получает список комментариев, связанных с указанным постом.
     *
     * @param postId идентификатор поста
     * @return список комментариев поста
     */
    public List<Comment> findCommentsByPostId(Integer postId) {
        return commentService.findByPostId(postId);
    }

    /**
     * Подсчитывает количество комментариев, связанных с указанным постом.
     *
     * @param postId идентификатор поста
     * @return количество комментариев
     */
    public int getCommentCountByPostId(int postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * Формирует отображение "ID поста -> список тегов" для списка постов.
     *
     * @param posts список постов
     * @return карта постов и их тегов
     */
    public Map<Integer, List<Tag>> getTagsForPosts(List<Post> posts) {
        Map<Integer, List<Tag>> result = new HashMap<>();
        for (Post post : posts) {
            List<Tag> tags = tagService.findTagsByPostId(post.getId());
            result.put(post.getId(), tags);
        }
        return result;
    }

    /**
     * Находит посты, связанные с указанным тегом.
     *
     * @param tagName имя тега для фильтрации
     * @return список постов с данным тегом
     */
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

    /**
     * Получает посты с пагинацией и опциональной фильтрацией по тегу.
     *
     * @param page номер страницы (начинается с 0)
     * @param size количество постов на странице
     * @param tag  (опционально) имя тега для фильтрации
     * @return список постов по заданным параметрам
     */
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

    /**
     * Подсчитывает общее количество постов с опциональной фильтрацией по тегу.
     *
     * @param tag (опционально) имя тега для фильтрации
     * @return количество постов
     */
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

    /**
     * Преобразует строку результата SQL-запроса в объект Post.
     *
     * @param rs результат SQL-запроса
     * @return объект Post
     * @throws SQLException если ошибка чтения из ResultSet
     */
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
