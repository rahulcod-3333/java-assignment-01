package com.internship.backend_assignment.service;

import com.internship.backend_assignment.dto.CommentRequest;
import com.internship.backend_assignment.dto.PostRequest;

public interface PostService {
     PostRequest createNewPost(PostRequest postRequest);

    CommentRequest addComment(CommentRequest commentRequest, Long postId);
}
