package com.example.blog.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.example.blog.model.Comment;

/**
 * Репозиторий для работы с сущностями комментариев {@link Comment}.
 * Наследуется от {@link CrudRepository} для базовых операций CRUD.
 */
public interface CommentRepository extends CrudRepository<Comment, Integer> {

    /**
     * Получить список комментариев по идентификатору поста.
     *
     * @param postId идентификатор поста
     * @return список комментариев, связанных с указанным постом
     */
    List<Comment> findByPostId(Integer postId);

    /**
     * Получить количество комментариев, связанных с указанным постом.
     *
     * @param postId идентификатор поста
     * @return количество комментариев для поста
     */
    int countByPostId(int postId);
}
