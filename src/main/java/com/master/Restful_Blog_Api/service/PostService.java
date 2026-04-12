package com.master.Restful_Blog_Api.service;


import com.master.Restful_Blog_Api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    Page<Post> getAllPosts(Pageable pageable, String search);
    List<Post> getAllPosts();
    Post addPost(Post thePost);
    Post getPostById(long theId);
    Post updatePostById(long theId, Post newPost);
    void deletePostById(long theId);
}
