package com.example.blog.controller;

import com.example.blog.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("Добавление комментария вызывает сервис и делает редирект")
    void testAddComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                .param("content", "Test comment"))
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).addComment(1, "Test comment");
    }

    @Test
    @DisplayName("Обновление комментария вызывает сервис и делает редирект")
    void testUpdateComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/5")
                .param("content", "Updated comment"))
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).updateComment(5, "Updated comment");
    }

    @Test
    @DisplayName("Удаление комментария вызывает сервис и делает редирект")
    void testDeleteComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/5/delete"))
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).deleteComment(5);
    }
}
