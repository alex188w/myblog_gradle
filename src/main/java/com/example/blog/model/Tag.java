package com.example.blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tags")
/**
 * Entity-класс, представляющий запись в таблице "tags".
 * Используется для хранения тегов к постам в блоге.
 */
public class Tag {
    @Id
    private Integer id;

    private String name;
}
