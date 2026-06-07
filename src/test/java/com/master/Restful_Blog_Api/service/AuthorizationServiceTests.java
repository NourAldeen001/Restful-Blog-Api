package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Comment;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.Role;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthorizationService Unit Tests")
class AuthorizationServiceTests {

    private AuthorizationService authorizationService;

    // Shared Fixtures
    User owner;
    User otherUser;
    User adminUser;
    Post samplePost;
    Comment sampleComment;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();

        owner = User.builder()
                .id(1L)
                .username("nour")
                .email("nour@example")
                .password("12344")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("hossam")
                .email("hossam@example")
                .password("12344")
                .role(Role.USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example")
                .password("12344")
                .role(Role.ADMIN)
                .build();

        samplePost = Post.builder()
                .id(1L)
                .title("Test Post Title")
                .content("Test Post Content")
                .author(owner)
                .build();


        sampleComment = Comment.builder()
                .id(1L)
                .content("Test Comment Content")
                .author(owner)
                .build();
    }

    @Nested
    @DisplayName("checkPostOwnership() Tests")
    class CheckPostOwnership {
        @Test
        @DisplayName("Should Doesn't Throw When Current User Is Owner")
        void should_notThrow_whenCurrentUserIsOwner() {
            assertThatNoException()
                    .isThrownBy(() -> authorizationService.checkPostOwnership(samplePost, owner));
        }


        @Test
        @DisplayName("Should Doesn't Throw When Current User Is Admin")
        void should_notThrow_whenCurrentUserIsAdmin() {
            assertThatNoException()
                    .isThrownBy(() -> authorizationService.checkPostOwnership(samplePost, adminUser));
        }

         @Test
        @DisplayName("Should Throw ForbiddenException When Current User Is Not Owner And Not Admin")
        void should_throwForbiddenException_whenCurrentUserIsNotOwnerAndIsNotAdmin() {
            assertThatThrownBy(() -> authorizationService.checkPostOwnership(samplePost, otherUser))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining(owner.getUsername());
        }
    }

    @Nested
    @DisplayName("checkCommentOwnership() Tests")
    class CheckCommentOwnership {
        @Test
        @DisplayName("Should Doesn't Throw When Current User Is Owner")
        void should_notThrow_whenCurrentUserIsOwner() {
            assertThatNoException()
                    .isThrownBy(() -> authorizationService.checkCommentOwnership(sampleComment, owner));
        }


        @Test
        @DisplayName("Should Doesn't Throw When Current User Is Admin")
        void should_notThrow_whenCurrentUserIsAdmin() {
            assertThatNoException()
                    .isThrownBy(() -> authorizationService.checkCommentOwnership(sampleComment, adminUser));
        }

        @Test
        @DisplayName("Should Throw ForbiddenException When Current User Is Not Owner And Not Admin")
        void should_throwForbiddenException_whenCurrentUserIsNotOwnerAndIsNotAdmin() {
            assertThatThrownBy(() -> authorizationService.checkCommentOwnership(sampleComment, otherUser))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining(owner.getUsername());
        }
    }


}