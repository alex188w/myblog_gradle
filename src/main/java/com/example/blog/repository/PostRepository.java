package com.example.blog.repository;

import org.springframework.data.repository.CrudRepository;
import com.example.blog.model.Post;
import java.util.List;

/**
 * Репозиторий для работы с сущностями постов {@link Post}.
 * Расширяет {@link CrudRepository} для базовых CRUD операций.
 */
public interface PostRepository extends CrudRepository<Post, Integer> {

    /**
     * Поиск постов, заголовки которых содержат указанную подстроку,
     * без учета регистра символов.
     *
     * @param titlePart подстрока для поиска в заголовках постов
     * @return список постов, заголовки которых содержат указанную подстроку
     */
    List<Post> findByTitleContainingIgnoreCase(String titlePart);
}