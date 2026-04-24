package com.internship.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private Long post_id;
    private Long author_id;
    private String content;
    private LocalDateTime created_at;
    private int depth_level;
    private boolean isBotAuthor;

}
