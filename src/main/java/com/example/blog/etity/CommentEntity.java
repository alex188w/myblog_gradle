package com.example.blog.etity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Комментарий, связанный с постом.
 */
@Data
@NoArgsConstructor
@Table("comments")
public class CommentEntity {

    @Id
    private Long id;

    @Column("post_id")
    private Long postId;

    private String content;
}
