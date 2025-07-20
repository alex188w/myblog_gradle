package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll(); // очищаем перед каждым тестом
    }

    @Test
    void testAddPost() throws Exception {
        mockMvc.perform(post("/posts")
                .param("title", "Integration Test Post")
                .param("preview", "Short preview")
                .param("text", "Long text")
                .param("tags", "spring,java")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        List<Post> posts = (List<Post>) postRepository.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Integration Test Post");
    }

    @Test
    void testEditPost() throws Exception {
        Post saved = postRepository.save(new Post(null, "Old Title", "Old Preview", null, "Old Text", 0));

        mockMvc.perform(post("/posts/" + saved.getId() + "/edit")
                .param("title", "Updated Title")
                .param("preview", "Updated Preview")
                .param("text", "Updated Text")
                .param("tags", "updated,spring")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + saved.getId()));

        Post updated = postRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void testDeletePost() throws Exception {
        Post saved = postRepository.save(new Post(null, "To be deleted", "preview", null, "text", 0));

        mockMvc.perform(post("/posts/" + saved.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        boolean exists = postRepository.findById(saved.getId()).isPresent();
        assertThat(exists).isFalse();
    }
}
