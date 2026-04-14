package com.master.Restful_Blog_Api.controller;

import com.master.Restful_Blog_Api.dto.CommentDTO;
import com.master.Restful_Blog_Api.dto.CreateCommentRequest;
import com.master.Restful_Blog_Api.dto.PagedResponse;
import com.master.Restful_Blog_Api.dto.UpdateCommentRequest;
import com.master.Restful_Blog_Api.entity.Comment;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.CommentNotBelongsToPostException;
import com.master.Restful_Blog_Api.mapper.CommentMapper;
import com.master.Restful_Blog_Api.repository.UserRepository;
import com.master.Restful_Blog_Api.service.CommentService;
import com.master.Restful_Blog_Api.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    private final CommentMapper commentMapper;

    private final PostService postService;

    private final UserRepository userRepository;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<PagedResponse<CommentDTO>> getAllComments(@PathVariable Long postId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                    @RequestParam(defaultValue = "desc") String sortDir) {
        // Verify post exists
        postService.getPostById(postId);

        // Build Sort
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Build Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> commentPage = commentService.getCommentsByPostId(postId, pageable);

        List<CommentDTO> commentDTOList = commentPage
                .stream()
                .map(commentMapper::toCommentDTO)
                .toList();

        PagedResponse<CommentDTO> response = PagedResponse.<CommentDTO>builder()
                .content(commentDTOList)
                .pageNumber(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .empty(commentPage.isEmpty())
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long postId,
                                                     @PathVariable Long commentId) {

        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists then get comment otherwise throw exception
        Comment comment = commentService.getCommentById(commentId);

        // Verify comment belongs to this post
        if(!comment.getPost().getId().equals(postId)) {
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        return ResponseEntity.ok(commentMapper.toCommentDTO(comment));
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long postId,
                                                 @Valid @RequestBody CreateCommentRequest createCommentRequest){

        Post post = postService.getPostById(postId);

        Comment comment = commentMapper.toEntity(createCommentRequest);

        comment.setPost(post);

        User authorComment = userRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test user not found"));

        authorComment.setId(2L);
        comment.setAuthor(authorComment);

        Comment saved = commentService.addComment(comment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentMapper.toCommentDTO(saved));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long postId,
                                                    @PathVariable Long commentId,
                                                    @Valid @RequestBody UpdateCommentRequest updateCommentRequest) {
        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists and belongs to post
        Comment comment = commentService.getCommentById(commentId);
        if(!comment.getPost().getId().equals(postId)) {
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        Comment newComment = commentMapper.toEntity(updateCommentRequest);
        Comment updated = commentService.updateComment(commentId, newComment);

        return ResponseEntity.ok(commentMapper.toCommentDTO(updated));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId,
                                              @PathVariable Long commentId) {

        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists and belongs to this post
        Comment comment = commentService.getCommentById(commentId);
        if(!comment.getPost().getId().equals(postId)) {
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}
