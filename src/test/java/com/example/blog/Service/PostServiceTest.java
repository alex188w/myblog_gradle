package com.example.blog.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostService;
import com.example.blog.service.TagService;

public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagService tagService;

    @Mock
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_ReturnsAllPosts() {
        Post p1 = new Post(1, "Title1", "Preview1", "img1", "Text1", 0);
        Post p2 = new Post(2, "Title2", "Preview2", "img2", "Text2", 5);

        when(postRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Post> result = postService.findAll();

        assertEquals(2, result.size());
        verify(postRepository).findAll();
    }

    @Test
    void findByTitle_ReturnsMatchingPosts() {
        String search = "test";
        Post p = new Post(1, "Test post", "prev", "img", "text", 0);
        when(postRepository.findByTitleContainingIgnoreCase(search)).thenReturn(List.of(p));

        List<Post> result = postService.findByTitle(search);

        assertEquals(1, result.size());
        assertEquals("Test post", result.get(0).getTitle());
    }

    @Test
    void findById_Found_ReturnsPost() {
        Post p = new Post(1, "Title", "prev", "img", "text", 0);
        when(postRepository.findById(1)).thenReturn(Optional.of(p));

        Post result = postService.findById(1);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
    }

    @Test
    void findById_NotFound_ReturnsNull() {
        when(postRepository.findById(99)).thenReturn(Optional.empty());

        Post result = postService.findById(99);

        assertNull(result);
    }

    @Test
    void save_WithTags_DeletesOldAndInsertsNewTags() {
        Post post = new Post(null, "New post", "prev", "img", "text", 0);
        Post savedPost = new Post(10, "New post", "prev", "img", "text", 0);

        when(postRepository.save(post)).thenReturn(savedPost);

        Tag tag1 = new Tag(1, "tag1");
        Tag tag2 = new Tag(2, "tag2");

        when(tagService.save(new Tag(null, "tag1"))).thenReturn(tag1);
        when(tagService.save(new Tag(null, "tag2"))).thenReturn(tag2);

        List<String> tags = List.of("tag1", "tag2");

        // Чтобы мок с tagService.save сработал с new Tag(null, "tag1") — нужен дополнительный матчинг
        // Можно использовать ArgumentMatchers.any() и Answer, но проще сделать такой вариант:
        doAnswer(invocation -> {
            Tag arg = invocation.getArgument(0);
            if ("tag1".equals(arg.getName())) return tag1;
            if ("tag2".equals(arg.getName())) return tag2;
            return null;
        }).when(tagService).save(any(Tag.class));

        Post result = postService.save(post, tags);

        assertEquals(savedPost.getId(), result.getId());

        verify(postRepository).save(post);
        verify(jdbcTemplate).update("DELETE FROM post_tags WHERE post_id = ?", savedPost.getId());
        verify(jdbcTemplate).update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)", savedPost.getId(), tag1.getId());
        verify(jdbcTemplate).update("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)", savedPost.getId(), tag2.getId());
    }

    @Test
    void save_WithoutTags_CallsSaveWithEmptyList() {
        Post post = new Post(null, "Title", "prev", "img", "text", 0);

        Post savedPost = new Post(5, "Title", "prev", "img", "text", 0);
        when(postRepository.save(post)).thenReturn(savedPost);

        Post result = postService.save(post, Collections.emptyList());

        assertEquals(savedPost.getId(), result.getId());
        verify(postRepository).save(post);
        verify(jdbcTemplate).update("DELETE FROM post_tags WHERE post_id = ?", savedPost.getId());
    }

    @Test
    void delete_CallsRepositoryDelete() {
        int id = 1;
        doNothing().when(postRepository).deleteById(id);

        postService.delete(id);

        verify(postRepository).deleteById(id);
    }

    @Test
    void findTagsByPostId_DelegatesToTagService() {
        int postId = 3;
        List<Tag> tags = List.of(new Tag(1, "tag"));
        when(tagService.findTagsByPostId(postId)).thenReturn(tags);

        List<Tag> result = postService.findTagsByPostId(postId);

        assertEquals(tags, result);
        verify(tagService).findTagsByPostId(postId);
    }

    @Test
    void findCommentsByPostId_DelegatesToCommentService() {
        int postId = 3;
        List<Comment> comments = List.of(mock(Comment.class));
        when(commentService.findByPostId(postId)).thenReturn(comments);

        List<Comment> result = postService.findCommentsByPostId(postId);

        assertEquals(comments, result);
        verify(commentService).findByPostId(postId);
    }

    @Test
    void getCommentCountByPostId_DelegatesToCommentRepository() {
        int postId = 5;
        when(commentRepository.countByPostId(postId)).thenReturn(7);

        int count = postService.getCommentCountByPostId(postId);

        assertEquals(7, count);
        verify(commentRepository).countByPostId(postId);
    }

    @Test
    void getTagsForPosts_ReturnsMap() {
        Post p1 = new Post(1, "t1", "p1", "img1", "text1", 0);
        Post p2 = new Post(2, "t2", "p2", "img2", "text2", 0);

        List<Tag> tags1 = List.of(new Tag(1, "tag1"));
        List<Tag> tags2 = List.of(new Tag(2, "tag2"));

        when(tagService.findTagsByPostId(1)).thenReturn(tags1);
        when(tagService.findTagsByPostId(2)).thenReturn(tags2);

        Map<Integer, List<Tag>> result = postService.getTagsForPosts(List.of(p1, p2));

        assertEquals(2, result.size());
        assertEquals(tags1, result.get(1));
        assertEquals(tags2, result.get(2));
    }

    @Test
    void findByTagName_ReturnsPosts() {
        String tag = "java";
        Post p = new Post(1, "Title", "prev", "img", "text", 0);

        // Мокаем jdbcTemplate.query с уточнением типа, чтобы не было warning
        when(jdbcTemplate.query(
            anyString(),
            ArgumentMatchers.<RowMapper<Post>>any(),
            eq(tag)
        )).thenReturn(List.of(p));

        List<Post> result = postService.findByTagName(tag);

        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).getTitle());
    }

    @Test
    void findPaginated_WithTag_ReturnsPosts() {
        int page = 0, size = 10;
        String tag = "spring";

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<Post>>any(),
                eq(tag), eq(size), eq(page * size)
        )).thenReturn(List.of(new Post(1, "title", "prev", "img", "text", 0)));

        List<Post> result = postService.findPaginated(page, size, tag);

        assertEquals(1, result.size());
    }

    @Test
    void findPaginated_WithoutTag_ReturnsPosts() {
        int page = 1, size = 5;

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<Post>>any(),
                eq(size), eq(page * size)
        )).thenReturn(List.of(new Post(2, "title2", "prev", "img", "text", 0)));

        List<Post> result = postService.findPaginated(page, size, null);

        assertEquals(1, result.size());
    }

    @Test
    void countPosts_WithTag_ReturnsCount() {
        String tag = "java";

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(tag))).thenReturn(7);

        int count = postService.countPosts(tag);

        assertEquals(7, count);
    }

    @Test
    void countPosts_WithoutTag_ReturnsCount() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(10);

        int count = postService.countPosts(null);

        assertEquals(10, count);
    }

}
