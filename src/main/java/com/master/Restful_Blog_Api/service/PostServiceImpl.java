package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.exception.PostNotFoundException;
import com.master.Restful_Blog_Api.exception.PostWithoutAuthorException;
import com.master.Restful_Blog_Api.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public Page<Post> getAllPosts(Pageable pageable, String search) {
        if(search != null && !search.isBlank()) {
            return postRepository.searchPosts(search, pageable);
        }
        else {
            return postRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional
    public Post addPost(Post post) {
        if(post.getAuthor() == null) {
            throw new PostWithoutAuthorException();
        }
        return postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post getPostById(long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @Override
    @Transactional
    public Post updatePostById(long id, Post newPost) {
        Post thePost = getPostById(id);
        thePost.setTitle(newPost.getTitle());
        thePost.setContent(newPost.getContent());;
        return postRepository.save(thePost);
    }

    @Override
    @Transactional
    public void deletePostById(long theId) {
        Post thePost = getPostById(theId);
        postRepository.delete(thePost);
    }
}
