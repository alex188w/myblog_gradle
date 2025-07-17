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
public class PostEntity {
    @Id
    private Long id;

    private String title;
    private String content;
    private String imageUrl;
    private int likes;

    @MappedCollection(idColumn = "post_id")
    private List<CommentEntity> comments = new ArrayList<>();
}
