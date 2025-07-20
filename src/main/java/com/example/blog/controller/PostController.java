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

    /**
     * Отображает список постов с поддержкой пагинации и фильтрации по тегу.
     *
     * @param tag   тег, по которому фильтруются посты (необязательный)
     * @param page  номер страницы (начинается с 0)
     * @param size  количество постов на странице
     * @param model модель для передачи данных в шаблон
     * @return шаблон posts.html
     */
    @GetMapping
    public String listPosts(
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        List<Post> posts = postService.findPaginated(page, size, tag);
        int totalPosts = postService.countPosts(tag);

        Map<Integer, List<Tag>> postTags = postService.getTagsForPosts(posts);
        Map<Integer, Integer> commentCounts = posts.stream()
                .collect(Collectors.toMap(Post::getId,
                        p -> postService.getCommentCountByPostId(p.getId())));

        model.addAttribute("posts", posts);
        model.addAttribute("postTags", postTags);
        model.addAttribute("commentCounts", commentCounts);
        model.addAttribute("selectedTag", tag);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("total", totalPosts);

        return "posts";
    }

    /**
     * Отображает отдельный пост со всеми тегами и комментариями.
     *
     * @param id    ID поста
     * @param model модель для передачи данных
     * @return шаблон post.html или редирект на /posts, если пост не найден
     */
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

    /**
     * Показывает форму добавления нового поста.
     *
     * @param model модель с новым объектом поста
     * @return шаблон add-post.html
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("tagsAsText", "");
        model.addAttribute("isNew", true);
        return "add-post";
    }

    /**
     * Сохраняет новый пост с тегами.
     *
     * @param post объект поста
     * @param tags строка с тегами (через запятую)
     * @return редирект на список постов
     */
    @PostMapping
    public String savePost(@ModelAttribute Post post,
                           @RequestParam(required = false) String tags) {
        List<String> tagNames = parseTags(tags);
        postService.save(post, tagNames);
        return "redirect:/posts";
    }

    /**
     * Показывает форму редактирования поста.
     *
     * @param id    ID редактируемого поста
     * @param model модель с данными поста и тегами
     * @return шаблон add-post.html или редирект, если пост не найден
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/posts";
        }

        List<Tag> tags = postService.findTagsByPostId(id);
        String tagsAsText = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute("post", post);
        model.addAttribute("tags", tags);
        model.addAttribute("tagsAsText", tagsAsText);
        model.addAttribute("isNew", false);

        return "add-post";
    }

    /**
     * Обновляет пост и его теги.
     *
     * @param id    ID поста
     * @param post  объект поста с обновлёнными данными
     * @param tags  строка с тегами (через запятую)
     * @return редирект на страницу поста
     */
    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Integer id,
                             @ModelAttribute Post post,
                             @RequestParam(required = false) String tags) {
        post.setId(id);
        List<String> tagNames = parseTags(tags);
        postService.save(post, tagNames);
        return "redirect:/posts/" + id;
    }

    /**
     * Удаляет пост.
     *
     * @param id ID удаляемого поста
     * @return редирект на список постов
     */
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Integer id) {
        postService.delete(id);
        return "redirect:/posts";
    }

    /**
     * Сохраняет или убирает лайк у поста.
     *
     * @param id   ID поста
     * @param like true — добавить лайк, false — убрать
     * @return редирект на страницу поста
     */
    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Integer id, @RequestParam boolean like) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/posts";
        }

        int currentLikes = post.getLikes();
        post.setLikes(like ? currentLikes + 1 : Math.max(0, currentLikes - 1));

        postService.save(post); // сохранение без тегов
        return "redirect:/posts/" + id;
    }

    /**
     * Обрабатывает загрузку изображений через multipart.
     *
     * @param file файл изображения
     * @return JSON с URL загруженного изображения
     */
    @PostMapping(value = "/uploadImage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "C:/myapp/uploads/";
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);
            Files.createDirectories(filepath.getParent());
            file.transferTo(filepath);

            String fileUrl = "/uploads/" + filename;

            return Map.of("url", fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный метод: преобразует строку с тегами в список имён.
     *
     * @param tags строка тегов через запятую
     * @return список отдельных тегов
     */
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
