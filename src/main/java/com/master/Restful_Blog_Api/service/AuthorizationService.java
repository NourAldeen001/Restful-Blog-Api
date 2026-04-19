package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Comment;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.Role;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public void checkPostOwnership(Post post, User currentUser) {

        boolean isOwner = post.getAuthor().getId().equals(currentUser.getId());

        if(!isOwner && !isAdmin(currentUser)) {
            throw new ForbiddenException(
                    "You don't have permissions to modify this post. It belongs to: " +
                    post.getAuthor().getUsername()
            );
        }
    }

    public void checkCommentOwnership(Comment comment, User currentUser) {

        boolean isOwner = comment.getAuthor().getId().equals(currentUser.getId());

        boolean isAdmin = isAdmin(currentUser);

        if(!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    "You don't have permissions to modify this comment. It belongs to: " +
                            comment.getAuthor().getUsername()
            );
        }
    }


    public boolean isAdmin(User currentUser) {
        return currentUser.getRole() == Role.ADMIN;
    }

    public void requireAdmin(User currentUser) {
        if(!isAdmin(currentUser)) {
            throw new ForbiddenException("This action requires administrator privileges");
        }
    }

}
