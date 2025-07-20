package com.example.blog.Service;

import com.example.blog.model.Comment;
import com.example.blog.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import com.example.blog.service.CommentService;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @Test
    void testAddComment_savesCommentWithCorrectData() {
        // act
        commentService.addComment(1, "Test comment"); // вызываем метод у объекта, а не у класса

        // assert
        verify(commentRepository).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();

        assertEquals(1, savedComment.getPostId());
        assertEquals("Test comment", savedComment.getContent());
        assertEquals("anon", savedComment.getAuthor());
        assertNotNull(savedComment.getCreatedAt());
    }

    @Test
    void testUpdateComment_updatesContentIfFound() {
        Comment existing = new Comment();
        existing.setId(10);
        existing.setContent("Old content");

        when(commentRepository.findById(10)).thenReturn(Optional.of(existing));

        commentService.updateComment(10, "New content");

        verify(commentRepository).save(commentCaptor.capture());
        assertEquals("New content", commentCaptor.getValue().getContent());
    }

    @Test
    void testUpdateComment_throwsIfNotFound() {
        when(commentRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> commentService.updateComment(999, "Updated"));

        assertEquals("Комментарий не найден", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testDeleteComment_callsRepositoryDeleteById() {
        commentService.deleteComment(5);
        verify(commentRepository).deleteById(5);
    }

    @Test
    void testFindByPostId_returnsComments() {
        List<Comment> comments = List.of(new Comment(), new Comment());
        when(commentRepository.findByPostId(1)).thenReturn(comments);

        List<Comment> result = commentService.findByPostId(1);

        assertEquals(2, result.size());
        verify(commentRepository).findByPostId(1);
    }
}
