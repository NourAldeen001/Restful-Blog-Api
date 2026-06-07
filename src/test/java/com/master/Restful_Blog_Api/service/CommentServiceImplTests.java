package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Comment;
import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.Role;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.CommentNotFoundException;
import com.master.Restful_Blog_Api.repository.CommentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Unit Tests")
class CommentServiceImplTests {

    // Dependencies

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    // Test Fixtures
    Comment sampleComment;
    Post samplePost;
    User authorPost;
    User authorComment;

    @BeforeEach
    void setUp() {
        authorPost = User.builder()
                .id(1L)
                .username("nour")
                .email("nour@example.com")
                .role(Role.USER)
                .build();

        authorComment = User.builder()
                .id(2L)
                .username("hossam")
                .email("hossam@example.com")
                .role(Role.USER)
                .build();

        samplePost = Post.builder()
                .id(1L)
                .title("Test Post Title")
                .content("Test Post Content")
                .author(authorPost)
                .build();

        sampleComment = Comment.builder()
                .id(1L)
                .content("Test Content Comment")
                .author(authorComment)
                .post(samplePost)
                .build();
    }

    @Nested
    @DisplayName("getCommentById() Tests")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Should Comment Return When Comment Exists")
        void should_commentReturn_whenCommentExists() {
            // Given
            Long commentId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.of(sampleComment));

            // When
           Comment result = commentService.getCommentById(commentId);

           // Then
           assertThat(result).isNotNull();
           assertThat(result.getContent()).isEqualTo("Test Content Comment");
           then(commentRepository).should(times(1)).findById(commentId);
           then(commentRepository).shouldHaveNoMoreInteractions();

        }

        @Test
        @DisplayName("Should Throw CommentNotFoundException When Comment Doesn't Exist")
        void should_throwCommentNotFoundException_whenCommentNotExists() {
            // Given
            Long commentId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // When && Then
            assertThatThrownBy(() -> commentService.getCommentById(commentId))
                    .isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should(times(1)).findById(commentId);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("addComment() Tests")
    class AddCommentTests {

        @Test
        @DisplayName("Should Comment Return When Add Comment")
        void should_commentReturn_whenAddComment() {
            // Given
            given(commentRepository.save(any(Comment.class))).willReturn(sampleComment);

            // When
            Comment result = commentService.addComment(sampleComment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Test Content Comment");
            then(commentRepository).should(times(1)).save(any(Comment.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("updateComment() Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should Throw CommentNotFoundException When Comment Doesn't Exist")
        void should_throwCommentNotFoundException_whenCommentNotExist() {
            // Given
            Long commentId = 1L;
            Comment updates = Comment.builder()
                    .content("Update Test Content Comment")
                    .build();

            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // When && Then
            assertThatThrownBy(() -> commentService.updateComment(commentId, updates))
                    .isInstanceOf(CommentNotFoundException.class);
            then(commentRepository).should(times(1)).findById(commentId);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Return Comment When Comment Exists")
        void should_returnComment_whenCommentExists() {
            // Given
            Long commentId = 1L;
            Comment updates = Comment.builder()
                    .content("Update Test Content Comment")
                    .build();

            given(commentRepository.findById(commentId)).willReturn(Optional.of(sampleComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            Comment result = commentService.updateComment(commentId, updates);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Update Test Content Comment");
            then(commentRepository).should(times(1)).findById(commentId);
            then(commentRepository).should(times(1)).save(any(Comment.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("deleteComment() Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should Throw CommentNotFoundException When Comment Doesn't Exist")
        void should_throwCommentNotFoundException_whenCommentNotExists() {
            // Given
            Long commentId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // When && Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId))
                    .isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should(times(1)).findById(commentId);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Delete Comment When Comment Exists")
        void should_deleteComment_whenCommentExists() {
            // Given
            Long commentId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.of(sampleComment));
            willDoNothing().given(commentRepository).delete(sampleComment);

            // When
            commentService.deleteComment(commentId);

            // Then
            then(commentRepository).should(times(1)).findById(commentId);
            then(commentRepository).should(times(1)).delete(sampleComment);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("getCommentsByPostId() Tests")
    class getCommentsByPostIdTests {

        @Test
        @DisplayName("Should Return All Comment Relate To Post When Post Exists")
        void should_commentReturn_whenCommentExists() {
            // Given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> commentPage = new PageImpl<>(List.of(sampleComment));
            given(commentRepository.findByPostId(postId, pageable)).willReturn(commentPage);

            // When
            Page<Comment> result = commentService.getCommentsByPostId(postId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).contains(sampleComment);
            then(commentRepository).should(times(1)).findByPostId(postId, pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();

        }
    }

}