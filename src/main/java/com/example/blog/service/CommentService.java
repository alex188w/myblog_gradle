package com.example.blog.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.blog.model.Comment;
import com.example.blog.repository.CommentRepository;

/**
 * Сервис для работы с комментариями.
 * Отвечает за добавление, обновление, удаление и получение комментариев.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    /**
     * Конструктор для внедрения зависимости репозитория комментариев.
     *
     * @param commentRepository репозиторий комментариев
     */
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Добавляет новый комментарий к посту.
     *
     * @param postId  идентификатор поста, к которому добавляется комментарий
     * @param content текст комментария
     */
    public void addComment(Integer postId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor("anon"); // или получить из SecurityContext, если есть авторизация
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    /**
     * Обновляет содержимое существующего комментария.
     *
     * @param commentId идентификатор комментария для обновления
     * @param content   новый текст комментария
     * @throws RuntimeException если комментарий с данным ID не найден
     */
    public void updateComment(Integer commentId, String content) {
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        if (existing != null) {
            existing.setContent(content);
            commentRepository.save(existing);
        }
    }

    /**
     * Удаляет комментарий по его идентификатору.
     *
     * @param commentId идентификатор комментария для удаления
     */
    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

    /**
     * Получает список комментариев, относящихся к заданному посту.
     *
     * @param postId идентификатор поста
     * @return список комментариев для поста
     */
    public List<Comment> findByPostId(Integer postId) {
        return commentRepository.findByPostId(postId);
    }
}
