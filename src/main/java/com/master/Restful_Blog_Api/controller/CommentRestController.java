package com.master.Restful_Blog_Api.controller;

import com.master.Restful_Blog_Api.dto.*;
import com.master.Restful_Blog_Api.entity.Comment;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.CommentNotBelongsToPostException;
import com.master.Restful_Blog_Api.mapper.CommentMapper;
import com.master.Restful_Blog_Api.service.AuthorizationService;
import com.master.Restful_Blog_Api.service.CommentService;
import com.master.Restful_Blog_Api.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class CommentRestController {

    private final CommentService commentService;

    private final CommentMapper commentMapper;

    private final PostService postService;

    private final AuthorizationService authorizationService;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<PagedResponse<CommentDTO>> getAllComments(@PathVariable Long postId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                    @RequestParam(defaultValue = "desc") String sortDir) {
        log.debug("Fetching comments: postId={}, page={}, size={}", postId, page, size);
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
        log.debug("Comments fetched: totalElements={}, totalPages={}", commentPage.getTotalElements(), commentPage.getTotalPages());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long postId,
                                                     @PathVariable Long commentId) {
        log.debug("Fetching comment: commentId={}, postId={}", commentId, postId);

        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists then get comment otherwise throw exception
        Comment comment = commentService.getCommentById(commentId);

        // Verify comment belongs to this post
        if(!comment.getPost().getId().equals(postId)) {
            log.warn("Comment does not belong to post: commentId={}, postId={}", commentId, postId);
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        return ResponseEntity.ok(commentMapper.toCommentDTO(comment));
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(@PathVariable Long postId,
                                                             @Valid @RequestBody CreateCommentRequest createCommentRequest,
                                                             @AuthenticationPrincipal User currentUser){
        log.info("Add comment request: postId={}, author={}", postId, currentUser.getUsername());
        Post post = postService.getPostById(postId);

        Comment comment = commentMapper.toEntity(createCommentRequest);

        comment.setPost(post);
        comment.setAuthor(currentUser);

        Comment saved = commentService.addComment(comment);
        log.info("Comment added successfully: commentId={}, postId={}, author={}",
                saved.getId(), postId, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Comment added successfully", commentMapper.toCommentDTO(saved)));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(@PathVariable Long postId,
                                                    @PathVariable Long commentId,
                                                    @Valid @RequestBody UpdateCommentRequest updateCommentRequest,
                                                    @AuthenticationPrincipal User currentUser) {
        log.info("Update comment request: commentId={}, postId={}, requestedBy={}",
                commentId, postId, currentUser.getUsername());
        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists and belongs to post
        Comment comment = commentService.getCommentById(commentId);
        if(!comment.getPost().getId().equals(postId)) {
            log.warn("Comment does not belong to post: commentId={}, postId={}", commentId, postId);
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        authorizationService.checkCommentOwnership(comment, currentUser);

        Comment newComment = commentMapper.toEntity(updateCommentRequest);
        Comment updated = commentService.updateComment(commentId, newComment);

        log.info("Comment updated successfully: commentId={}, updatedBy={}", commentId, currentUser.getUsername());

        return ResponseEntity.ok(ApiResponse.ok("Comment updated successfully", commentMapper.toCommentDTO(updated)));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long postId,
                                              @PathVariable Long commentId,
                                              @AuthenticationPrincipal User currentUser) {
        log.info("Delete comment request: commentId={}, postId={}, requestedBy={}",
                commentId, postId, currentUser.getUsername());
        // Verify post exists
        postService.getPostById(postId);

        // Verify comment exists and belongs to this post
        Comment comment = commentService.getCommentById(commentId);
        if(!comment.getPost().getId().equals(postId)) {
            log.warn("Comment does not belong to post: commentId={}, postId={}", commentId, postId);
            throw new CommentNotBelongsToPostException(commentId, postId);
        }

        authorizationService.checkCommentOwnership(comment, currentUser);

        commentService.deleteComment(commentId);
        log.info("Comment deleted successfully: commentId={}, deletedBy={}", postId, currentUser.getUsername());

        return ResponseEntity.ok(ApiResponse.deleted("Comment deleted successfully"));
    }
}
