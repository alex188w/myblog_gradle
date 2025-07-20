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

    /**
     * Добавляет новый комментарий к посту.
     *
     * @param postId  ID поста, к которому добавляется комментарий
     * @param content Текст комментария
     * @return Редирект обратно на страницу поста
     */
    @PostMapping
    public String addComment(@PathVariable Integer postId,
                             @RequestParam String content) {
        commentService.addComment(postId, content);
        return "redirect:/posts/" + postId;
    }

    /**
     * Обновляет содержимое существующего комментария.
     *
     * @param postId     ID поста (для возврата)
     * @param commentId  ID редактируемого комментария
     * @param content    Новый текст комментария
     * @return Редирект обратно на страницу поста
     */
    @PostMapping("/{commentId}")
    public String updateComment(@PathVariable Integer postId,
                                @PathVariable Integer commentId,
                                @RequestParam String content) {
        commentService.updateComment(commentId, content);
        return "redirect:/posts/" + postId;
    }

    /**
     * Удаляет комментарий.
     *
     * @param postId    ID поста (для возврата)
     * @param commentId ID удаляемого комментария
     * @return Редирект обратно на страницу поста
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Integer postId,
                                @PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/posts/" + postId;
    }
}
