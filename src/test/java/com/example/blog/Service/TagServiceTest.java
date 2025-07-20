package com.example.blog.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.blog.model.Tag;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.TagService;

public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findTagsByPostId_ReturnsListOfTags() {
        // Подготовка данных
        int postId = 1;
        Tag tag1 = new Tag();
        tag1.setId(1);
        tag1.setName("java");

        Tag tag2 = new Tag();
        tag2.setId(2);
        tag2.setName("spring");

        List<Tag> expectedTags = List.of(tag1, tag2);

        // Мокаем jdbcTemplate.query
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<Tag>>any(),
                eq(postId))).thenReturn(expectedTags);

        // Выполнение тестируемого метода
        List<Tag> actualTags = tagService.findTagsByPostId(postId);

        // Проверки
        assertNotNull(actualTags);
        assertEquals(2, actualTags.size());
        assertEquals("java", actualTags.get(0).getName());
        assertEquals("spring", actualTags.get(1).getName());

        // Проверяем, что jdbcTemplate.query вызван с правильными аргументами
        verify(jdbcTemplate, times(1)).query(
                anyString(),
                ArgumentMatchers.<RowMapper<Tag>>any(),
                eq(postId));
    }

    @Test
    void save_WhenTagExists_ReturnsExistingTag() {
        Tag inputTag = new Tag();
        inputTag.setName("java");

        Tag existingTag = new Tag();
        existingTag.setId(1);
        existingTag.setName("java");

        // Мокаем поведение репозитория: тег найден
        when(tagRepository.findByName("java")).thenReturn(existingTag);

        Tag result = tagService.save(inputTag);

        assertNotNull(result);
        assertEquals(existingTag.getId(), result.getId());
        assertEquals(existingTag.getName(), result.getName());

        // Проверяем, что save у репозитория не вызвался, так как тег уже есть
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void save_WhenTagNotExists_SavesAndReturnsTag() {
        Tag inputTag = new Tag();
        inputTag.setName("newtag");

        Tag savedTag = new Tag();
        savedTag.setId(2);
        savedTag.setName("newtag");

        // Мокаем поведение репозитория: тег не найден
        when(tagRepository.findByName("newtag")).thenReturn(null);
        when(tagRepository.save(inputTag)).thenReturn(savedTag);

        Tag result = tagService.save(inputTag);

        assertNotNull(result);
        assertEquals(savedTag.getId(), result.getId());
        assertEquals(savedTag.getName(), result.getName());

        // Проверяем, что save у репозитория вызвался ровно один раз
        verify(tagRepository, times(1)).save(inputTag);
    }
}
