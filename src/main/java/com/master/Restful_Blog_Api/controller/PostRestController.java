package com.master.Restful_Blog_Api.controller;

import com.master.Restful_Blog_Api.dto.*;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.mapper.PostMapper;
import com.master.Restful_Blog_Api.service.AuthorizationService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PostRestController {

    private final PostService postService;

    private final PostMapper postMapper;

    private final AuthorizationService authorizationService;


    @GetMapping("/posts")
    public ResponseEntity<PagedResponse<PostDTO>> getAllPosts(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "createdAt") String sortBy,
                                                              @RequestParam(defaultValue = "desc") String sortDir,
                                                              @RequestParam(required = false) String search) {
        // DEBUG: Useful during development but too noisy for production
        log.debug("Fetching posts: page={}, size={}, sortBy={}, sortDir={}, search={}",
                page, size, sortBy, sortDir, search);

        // Build Sort
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get paginated posts
        Page<Post> postPage = postService.getAllPosts(pageable, search);

        // Convert to DTOs
        List<PostDTO> postDTOList = postPage
                .stream()
                .map(postMapper::toPostDTO)
                .toList();

        PagedResponse<PostDTO> response = PagedResponse.<PostDTO>builder()
                .content(postDTOList)
                .pageNumber(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .empty(postPage.isEmpty())
                .build();

        log.debug("Posts fetched: totalElements={}, totalPages={}", postPage.getTotalElements(), postPage.getTotalPages());

        return ResponseEntity.ok(response);

    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable("id") Long theId) {
        log.debug("Fetching post by id={}", theId);
        PostDTO post = postMapper.toPostDTO(postService.getPostById(theId));
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostDTO>> addPost(@Valid @RequestBody CreatePostRequest createPostRequest,
                                                       @AuthenticationPrincipal User currentUser) {
        log.info("Create post request: title={}, author={}", createPostRequest.getTitle(), currentUser.getUsername());
        Post post = postMapper.toEntity(createPostRequest);

        post.setAuthor(currentUser);

        Post saved = postService.addPost(post);
        log.info("Post created successfully: postId={}, title={}, author={}", saved.getId(), saved.getTitle(), currentUser.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Post added successfully", postMapper.toPostDTO(saved)));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> updatePostById(@PathVariable("id") Long theId,
                                                  @Valid @RequestBody UpdatePostRequest updatePostRequest,
                                                  @AuthenticationPrincipal User currentUser) {
        log.info("Update post request: postId={}, requestedBy={}", theId, currentUser.getUsername());
        Post existingPost = postService.getPostById(theId);
        authorizationService.checkPostOwnership(existingPost, currentUser);
        Post post = postMapper.toEntity(updatePostRequest);
        Post updated = postService.updatePostById(theId, post);
        log.info("Post updated successfully: postId={}, updatedBy={}", theId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Post updated successfully", postMapper.toPostDTO(updated)));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePostById(@PathVariable("id") Long theId,
                                               @AuthenticationPrincipal User currentUser) {
        log.info("Delete post request: postId={}, requestedBy={}", theId, currentUser.getUsername());
        Post existingPost = postService.getPostById(theId);
        authorizationService.checkPostOwnership(existingPost, currentUser);
        postService.deletePostById(theId);
        log.info("Post deleted successfully: postId={}, deletedBy={}", theId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.deleted("Post deleted successfully"));
    }
}
