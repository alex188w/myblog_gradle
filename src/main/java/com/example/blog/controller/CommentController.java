package com.example.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.blog.service.CommentService;

@Controller
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public String addComment(@PathVariable Integer postId,
                             @RequestParam String content) {
        commentService.addComment(postId, content);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{commentId}")
    public String updateComment(@PathVariable Integer postId,
                                @PathVariable Integer commentId,
                                @RequestParam String content) {
        commentService.updateComment(commentId, content);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Integer postId,
                                @PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/posts/" + postId;
    }
}
