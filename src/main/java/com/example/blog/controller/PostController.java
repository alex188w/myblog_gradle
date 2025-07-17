package com.example.blog.controller;

import com.example.blog.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.blog.model.Post;
import com.example.blog.model.Tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.example.blog.model.Comment;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String listPosts(@RequestParam(defaultValue = "") String search,
            Model model) {
        List<Post> posts = postService.findByTitle(search);
        model.addAttribute("posts", posts);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", 0); // временно
        model.addAttribute("pageSize", 10);
        model.addAttribute("total", posts.size());
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
}
