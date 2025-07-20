package com.example.blog.service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.blog.model.Tag;
import com.example.blog.repository.TagRepository;

/**
 * Сервис для работы с тегами.
 * Отвечает за получение тегов, связанных с постами, и сохранение тегов.
 */
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param tagRepository репозиторий тегов
     * @param jdbcTemplate  JdbcTemplate для выполнения SQL-запросов
     */
    public TagService(TagRepository tagRepository, JdbcTemplate jdbcTemplate) {
        this.tagRepository = tagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получает список тегов, связанных с конкретным постом.
     *
     * @param postId идентификатор поста
     * @return список тегов, связанных с постом
     */
    public List<Tag> findTagsByPostId(Integer postId) {
        String sql = "SELECT t.id, t.name FROM tags t " +
                     "JOIN post_tags pt ON t.id = pt.tag_id " +
                     "WHERE pt.post_id = ?";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Tag(rs.getInt("id"), rs.getString("name")),
                postId);
    }

    /**
     * Сохраняет тег, если он еще не существует.
     * Если тег с таким именем уже есть, возвращает существующий.
     *
     * @param tag тег для сохранения
     * @return сохраненный или уже существующий тег
     */
    public Tag save(Tag tag) {
        Tag existing = tagRepository.findByName(tag.getName());
        if (existing != null) {
            return existing;
        }
        return tagRepository.save(tag);
    }
}
