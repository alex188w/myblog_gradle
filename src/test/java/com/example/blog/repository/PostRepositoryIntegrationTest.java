package com.example.blog.repository;

import com.example.blog.model.Post;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void cleanDatabase() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("Поиск постов по подстроке в заголовке без учета регистра")
    void testFindByTitleContainingIgnoreCase() {
        // Arrange
        Post post1 = new Post();
        post1.setTitle("Spring Boot Guide");
        post1.setPreview("Preview 1");
        post1.setImageUrl("/img/spring.jpg");
        post1.setText("Some text");
        post1.setLikes(0);
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Another Post");
        post2.setPreview("Preview 2");
        post2.setImageUrl("/img/another.jpg");
        post2.setText("More text");
        post2.setLikes(0);
        postRepository.save(post2);

        // Act
        List<Post> results = postRepository.findByTitleContainingIgnoreCase("spring");

        // Assert
        assertThat(results)
            .hasSize(1)
            .first()
            .extracting(Post::getTitle)
            .isEqualTo("Spring Boot Guide");
    }
}
