package com.example.blog.repository;

import org.springframework.data.repository.CrudRepository;
import com.example.blog.model.Tag;

/**
 * Репозиторий для работы с сущностями тегов {@link Tag}.
 * Расширяет {@link CrudRepository} для базовых CRUD операций.
 */
public interface TagRepository extends CrudRepository<Tag, Integer> {

    /**
     * Поиск тега по его уникальному имени.
     *
     * @param name имя тега
     * @return тег с указанным именем или null, если тег не найден
     */
    Tag findByName(String name);
}
