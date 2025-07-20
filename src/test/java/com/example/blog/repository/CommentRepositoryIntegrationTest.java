package com.example.blog.repository;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentRepositoryIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("Найти комментарии по ID поста")
    void testFindByPostId() {
        // 1. Создаём пост
        Post post = new Post();
        post.setTitle("Тестовый заголовок");
        post.setPreview("Краткий анонс");
        post.setImageUrl("/img/test.jpg");
        post.setText("Полный текст поста");
        post.setLikes(0);
        Post savedPost = postRepository.save(post);

        // 2. Сохраняем комментарий
        Comment comment = new Comment();
        comment.setPostId(savedPost.getId());
        comment.setAuthor("Alex");
        comment.setContent("Комментарий к посту");
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // 3. Проверка
        List<Comment> comments = commentRepository.findByPostId(savedPost.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getAuthor()).isEqualTo("Alex");
    }

    @Test
    @DisplayName("Подсчитать количество комментариев к посту")
    void testCountByPostId() {
        Post post = new Post();
        post.setTitle("Заголовок 2");
        post.setPreview("Превью 2");
        post.setImageUrl("/img/img2.jpg");
        post.setText("Текст 2");
        post.setLikes(0);
        Post savedPost = postRepository.save(post);

        Comment comment1 = new Comment();
        comment1.setPostId(savedPost.getId());
        comment1.setAuthor("User1");
        comment1.setContent("Комментарий 1");
        comment1.setCreatedAt(LocalDateTime.now());

        Comment comment2 = new Comment();
        comment2.setPostId(savedPost.getId());
        comment2.setAuthor("User2");
        comment2.setContent("Комментарий 2");
        comment2.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        int count = commentRepository.countByPostId(savedPost.getId());
        assertThat(count).isEqualTo(2);
    }
}
