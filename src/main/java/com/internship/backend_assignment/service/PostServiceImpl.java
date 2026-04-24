package com.internship.backend_assignment.service;

import com.internship.backend_assignment.exception.ResourceNotFoundException;
import com.internship.backend_assignment.exception.TooManyRequestException;
import com.internship.backend_assignment.dto.CommentRequest;
import com.internship.backend_assignment.dto.PostRequest;
import com.internship.backend_assignment.entity.Comment;
import com.internship.backend_assignment.entity.Post;
import com.internship.backend_assignment.repository.CommentRepository;
import com.internship.backend_assignment.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final InteractionService guardrailService;
    private final NotificationServiceImp notificationService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @Override
    public PostRequest createNewPost(PostRequest postRequest) {
        Post post = Post.builder()
                .author_id(postRequest.getAuthor_id())
                .isBotAuthor(postRequest.is_Bot())
                .created_at(LocalDateTime.now())
                .content(postRequest.getContent())
                .build();

        post = postRepository.save(post);
        PostRequest response = modelMapper.map(post, PostRequest.class);
        response.set_Bot(post.isBotAuthor());
        return response;    }

    @Transactional
    @Override
    public CommentRequest addComment(CommentRequest commentRequest, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        Long humanId = post.isBotAuthor() ? null : post.getAuthor_id() ;

        int depth = 1;
        if(commentRequest.getParentCommentId() != null){
            depth = commentRepository.findById(commentRequest.getParentCommentId())
                    .map(parentComment -> parentComment.getDepth_level()+1)
                    .orElseThrow(()-> new ResourceNotFoundException("Parent Comment id is not found"));

        }


        if (depth > 20) {
            throw new TooManyRequestException("depth exceeds limit");
        }

        if (commentRequest.is_bot()) {
            guardrailService.validateBotInteraction(postId, commentRequest.getAuthor_id(), humanId);
        }
        Comment comment = Comment.builder()
                .post_id(postId)
                .author_id(commentRequest.getAuthor_id())
                .isBotAuthor(commentRequest.is_bot())
                .content(commentRequest.getContent())
                .depth_level(depth)
                .created_at(LocalDateTime.now())
                .build();

        try {
            comment = commentRepository.save(comment);
        }
        catch (Exception e){
            redisTemplate.opsForValue().decrement("post:" + postId + ":bot_count");
            throw new RuntimeException("Failed to save in DB as well as redis, rollback");
        }

        if (commentRequest.is_bot() && humanId != null) {
            notificationService.processBotNotification(humanId, commentRequest.getAuthor_id());
        }

        String interactionType = commentRequest.is_bot() ? "BOT_REPLY" : "HUMAN_COMMENT";
        guardrailService.incrementViralityScore(postId, interactionType);

        CommentRequest response = modelMapper.map(comment, CommentRequest.class);
        response.set_bot(comment.isBotAuthor());
        return response;


    }
}