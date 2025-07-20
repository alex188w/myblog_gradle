package com.example.blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
/**
 * Entity-класс, представляющий запись в таблице "comments".
 * Используется для хранения комментариев к постам в блоге.
 */
public class Comment {
    @Id
    private Integer id;

    private Integer postId;

    private String author;

    private String content;

    private LocalDateTime createdAt;
}
