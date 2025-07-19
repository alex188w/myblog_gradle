package com.example.blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
public class Post {

    @Id
    private Integer id;

    private String title;

    private String preview;

    private String imageUrl;

    private String text;

    private int likes;
}
