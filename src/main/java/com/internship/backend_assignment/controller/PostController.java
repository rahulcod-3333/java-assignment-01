package com.internship.backend_assignment.controller;


import com.internship.backend_assignment.dto.CommentRequest;
import com.internship.backend_assignment.dto.PostRequest;
import com.internship.backend_assignment.entity.InteractionType;
import com.internship.backend_assignment.service.InteractionService;
import com.internship.backend_assignment.service.PostServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceImpl postService;
    private final InteractionService guardrailService;
    @PostMapping
    public ResponseEntity<PostRequest> createNewPost(@RequestBody PostRequest postRequest){
        return ResponseEntity.ok(postService.createNewPost(postRequest));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentRequest> createCommentToPost(@RequestBody CommentRequest commentRequest , @PathVariable Long postId){
        return ResponseEntity.ok(postService.addComment(commentRequest ,postId));

    }
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> LikePost(@PathVariable Long postId){

        guardrailService.incrementViralityScore(postId, String.valueOf(InteractionType.HUMAN_LIKE));
        return ResponseEntity.ok("Post is liked increase virality score");
    }



}
