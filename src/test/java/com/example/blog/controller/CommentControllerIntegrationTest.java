package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();

        post = postRepository.save(new Post(null, "Post for comments", "preview", null, "text", 0));
    }

    @Test
    void testAddComment() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments")
                .param("content", "Test comment content")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        List<Comment> comments = commentRepository.findByPostId(post.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Test comment content");
    }

    @Test
    void testUpdateComment() throws Exception {
        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setAuthor("TestAuthor");
        comment.setContent("To be deleted");
        comment.setCreatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId())
                .param("content", "Updated comment content")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("Updated comment content");
    }

    @Test
    void testDeleteComment() throws Exception {
        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setAuthor("TestAuthor");
        comment.setContent("To be deleted");
        comment.setCreatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);

        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        boolean exists = commentRepository.findById(comment.getId()).isPresent();
        assertThat(exists).isFalse();
    }
}
