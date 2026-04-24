package com.internship.backend_assignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    private Long author_id;
    @JsonProperty("is_bot")
    private boolean is_bot;
    private String content;
    private Long parentCommentId;
}
