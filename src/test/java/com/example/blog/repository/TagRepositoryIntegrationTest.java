package com.example.blog.repository;

import com.example.blog.model.Tag;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TagRepositoryIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void cleanDatabase() {
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("Поиск тега по имени")
    void testFindByName() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("spring");
        tagRepository.save(tag);

        // Act
        Tag result = tagRepository.findByName("spring");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("spring");
    }

    @Test
    @DisplayName("Поиск несуществующего тега возвращает null")
    void testFindByNameReturnsNullIfNotFound() {
        // Act
        Tag result = tagRepository.findByName("nonexistent");

        // Assert
        assertThat(result).isNull();
    }
}
