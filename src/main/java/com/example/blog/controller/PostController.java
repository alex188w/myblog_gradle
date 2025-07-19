package com.example.blog.controller;

import com.example.blog.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.blog.model.Post;
import com.example.blog.model.Tag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.blog.model.Comment;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String listPosts(@RequestParam(value = "tag", required = false) String tag,
            Model model) {
        List<Post> posts = (tag != null)
                ? postService.findByTagName(tag)
                : postService.findAll();

        Map<Integer, List<Tag>> postTags = postService.getTagsForPosts(posts);

        Map<Integer, Integer> commentCounts = posts.stream()
                .collect(Collectors.toMap(Post::getId,
                        p -> postService.getCommentCountByPostId(p.getId())));

        model.addAttribute("posts", posts);
        model.addAttribute("postTags", postTags);
        model.addAttribute("commentCounts", commentCounts);
        model.addAttribute("selectedTag", tag);

        return "posts";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Integer id, Model model) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/posts";
        }

        List<Tag> tags = postService.findTagsByPostId(id);
        List<Comment> comments = postService.findCommentsByPostId(id);

        model.addAttribute("post", post);
        model.addAttribute("tags", tags);
        model.addAttribute("comments", comments);
        model.addAttribute("isNew", false);

        return "post";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("tagsAsText", ""); // пустая строка для формы
        return "add-post";
    }

    @PostMapping
    public String savePost(@ModelAttribute Post post,
            @RequestParam(required = false) String tags) {
        List<String> tagNames = parseTags(tags);
        postService.save(post, tagNames);
        return "redirect:/posts";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/posts";
        }
        List<Tag> tags = postService.findTagsByPostId(id);
        model.addAttribute("post", post);
        model.addAttribute("tags", tags);
        return "add-post";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Integer id,
            @ModelAttribute Post post,
            @RequestParam(required = false) String tags) {
        post.setId(id);
        List<String> tagNames = parseTags(tags);
        postService.save(post, tagNames);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Integer id) {
        postService.delete(id);
        return "redirect:/posts";
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Integer id, @RequestParam boolean like) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/posts";
        }

        int currentLikes = post.getLikes();
        post.setLikes(like ? currentLikes + 1 : Math.max(0, currentLikes - 1));

        postService.save(post); // Без тегов, сделаем перегрузку метода в PostService
        return "redirect:/posts/" + id;
    }

    // Для загрузки изображений
    @PostMapping(value = "/uploadImage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "C:/myapp/uploads/";
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);
            Files.createDirectories(filepath.getParent());
            file.transferTo(filepath);

            // Этот путь должен совпадать с тем, как раздаём /uploads/**
            String fileUrl = "/uploads/" + filename;

            return Map.of("url", fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }

}
