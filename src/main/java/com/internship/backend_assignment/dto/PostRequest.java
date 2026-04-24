package com.internship.backend_assignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private Long author_id;
    @JsonProperty("is_bot")
    private boolean is_Bot;
    private String content;
}
