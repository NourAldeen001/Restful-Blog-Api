package com.master.Restful_Blog_Api.mapper;

import com.master.Restful_Blog_Api.dto.CreatePostRequest;
import com.master.Restful_Blog_Api.dto.PostDTO;
import com.master.Restful_Blog_Api.dto.UpdatePostRequest;
import com.master.Restful_Blog_Api.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostDTO toPostDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                .commentCount(post.getComments() != null ? post.getComments().size() : 0)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public Post toEntity(CreatePostRequest createPostRequest){
        return Post.builder()
                .title(createPostRequest.getTitle())
                .content(createPostRequest.getContent())
                .build();
    }

    public Post toEntity(UpdatePostRequest updatePostRequest){
        return Post.builder()
                .title(updatePostRequest.getTitle())
                .content(updatePostRequest.getContent())
                .build();
    }

}
