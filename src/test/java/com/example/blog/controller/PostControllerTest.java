package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import com.example.blog.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("GET /posts — отображение списка постов")
    void listPosts_ReturnsPostListView() throws Exception {
        Post post = new Post(1, "Заголовок", "Превью", "image.jpg", "Текст", 3);
        Mockito.when(postService.findPaginated(0, 10, null)).thenReturn(List.of(post));
        Mockito.when(postService.countPosts(null)).thenReturn(1);
        Mockito.when(postService.getTagsForPosts(anyList())).thenReturn(Map.of(1, List.of()));
        Mockito.when(postService.getCommentCountByPostId(1)).thenReturn(5);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts", "postTags", "commentCounts", "currentPage", "pageSize",
                        "total"));
    }

    @Test
    @DisplayName("GET /posts/1 — отображение отдельного поста")
    void viewPost_ExistingId_ReturnsPostView() throws Exception {
        Post post = new Post(1, "Заголовок", "Превью", "image.jpg", "Текст", 2);
        Mockito.when(postService.findById(1)).thenReturn(post);
        Mockito.when(postService.findTagsByPostId(1)).thenReturn(List.of(new Tag(1, "Java")));
        Mockito.when(postService.findCommentsByPostId(1)).thenReturn(List.of());

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeExists("post", "tags", "comments", "isNew"));
    }

    @Test
    @DisplayName("GET /posts/999 — редирект, если пост не найден")
    void viewPost_NotFound_Redirects() throws Exception {
        Mockito.when(postService.findById(999)).thenReturn(null);

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("GET /posts/add — отображение формы добавления")
    void showAddForm_ReturnsAddPostView() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attributeExists("post", "tagsAsText", "isNew"));
    }

    @Test
    @DisplayName("POST /posts — сохранение нового поста")
    void savePost_RedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                .param("title", "Новый пост")
                .param("preview", "Кратко")
                .param("imagePath", "/img.jpg")
                .param("content", "Текст поста")
                .param("tags", "Java,Spring"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        Mockito.verify(postService).save(any(Post.class), eq(List.of("Java", "Spring")));
    }

    @Test
    @DisplayName("GET /posts/1/edit — форма редактирования")
    void showEditForm_ReturnsAddPostView() throws Exception {
        Post post = new Post(1, "Редактируемый пост", "prev", "img", "text", 0);
        Mockito.when(postService.findById(1)).thenReturn(post);
        Mockito.when(postService.findTagsByPostId(1)).thenReturn(List.of(new Tag(1, "Test")));

        mockMvc.perform(get("/posts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attributeExists("post", "tags", "tagsAsText", "isNew"));
    }

    @Test
    @DisplayName("POST /posts/1/edit — обновление поста")
    void updatePost_RedirectsToPost() throws Exception {
        mockMvc.perform(post("/posts/1/edit")
                .param("title", "Обновлённый пост")
                .param("preview", "prev")
                .param("imagePath", "/img.jpg")
                .param("content", "text")
                .param("tags", "Java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        Mockito.verify(postService).save(Mockito.argThat(p -> p.getId() == 1), eq(List.of("Java")));
    }

    @Test
    @DisplayName("POST /posts/1/delete — удаление поста")
    void deletePost_RedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        Mockito.verify(postService).delete(1);
    }
}
