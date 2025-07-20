package com.example.blog.etity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
/**
 * Entity-класс, представляющий запись в таблице "posts".
 * Используется для хранения информации о постах в блоге.
 */
public class PostEntity {
    @Id
    private Long id;

    private String title;
    private String content;
    private String imageUrl;
    private int likes;

    /**
     * Список комментариев, связанных с этим постом.
     * Связь происходит по колонке "post_id" в таблице комментариев.
     */
    @MappedCollection(idColumn = "post_id")
    private List<CommentEntity> comments = new ArrayList<>();
}
