package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Post;
import com.master.Restful_Blog_Api.entity.Role;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.PostNotFoundException;
import com.master.Restful_Blog_Api.exception.PostWithoutAuthorException;
import com.master.Restful_Blog_Api.repository.PostRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
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

@ExtendWith(MockitoExtension.class) //
@DisplayName("PostServiceImpl Unit Test")
class PostServiceImplTests {

    /// Dependencies

    @Mock
    private PostRepository postRepository; // mock it

    @InjectMocks
    private PostServiceImpl postService; // under test

    /// Test Fixtures
     Post samplePost;
     User author;

     @BeforeEach
     void setUp() {
         author = User.builder()
                 .id(1L)
                 .username("nour")
                 .email("nour@example.com")
                 .role(Role.USER)
                 .build();

         samplePost = Post.builder()
                 .id(1L)
                 .title("Test Post")
                 .content("Test Post Content")
                 .author(author)
                 .build();
     }

     @Nested
     @DisplayName("getPostById() Tests")
     class GetPostByIdTests {

         @Test
         @DisplayName("Should Post Return When Post Exists")
         void should_returnPost_whenPostExists() {
             // Given
             Long postId = 1L;
             BDDMockito.given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));

             // When
             Post result = postService.getPostById(postId);

             // Then
             assertThat(result.getTitle()).isEqualTo("Test Post");
             assertThat(result.getContent()).isEqualTo("Test Post Content");
             assertThat(result.getAuthor().getUsername()).isEqualTo("nour");
             then(postRepository).should(times(1)).findById(postId);
             then(postRepository).shouldHaveNoMoreInteractions();
         }

         @Test
         @DisplayName("Should Throw PostNotFoundException When Post Doesn't Exist")
         void should_throwPostNotFoundException_whenPostNotExists() {
             // Given
             Long postId = 999L;
             given(postRepository.findById(postId)).willReturn(Optional.empty());

             // When && Then
             assertThatThrownBy(() -> postService.getPostById(postId))
                     .isInstanceOf(PostNotFoundException.class);

             then(postRepository).should(times(1)).findById(postId);
             then(postRepository).shouldHaveNoMoreInteractions();
         }
     }

    @Nested
    @DisplayName("addPost() Tests")
    class AddPostTests {

        @Test
        @DisplayName("Should Add Post And Return When Post Have Author")
        void should_addPostAndReturn_whenPostHaveAuthor() {
            // Given
            given(postRepository.save(any(Post.class))).willReturn(samplePost);

            // When
            Post result = postService.addPost(samplePost);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Post");
            assertThat(result.getContent()).isEqualTo("Test Post Content");
            then(postRepository).should(times(1)).save(samplePost);
            then(postRepository).shouldHaveNoMoreInteractions();

        }

        @Test
        @DisplayName("Should Throw PostWithoutAuthorException When Author Is Null")
        void should_throwPostWithoutAuthorException_whenAuthorIsNull() {
            // Given
            Post noAuthor = Post.builder()
                    .title("Post")
                    .content("Content")
                    .author(null)
                    .build();

            // When && Then
            assertThatThrownBy(() -> postService.addPost(noAuthor))
                    .isInstanceOf(PostWithoutAuthorException.class);

            // then(postRepository).should(times(0)).save(noAuthor); OR
            // then(postRepository).should(never()).save(any()); OR
            then(postRepository).should(never()).save(any());
            then(postRepository).shouldHaveNoInteractions();
        }

    }

    @Nested
    @DisplayName("updatePostById() Tests")
    class UpdatePostTests {
        /**
         * willReturn(value) → "Always return this fixed value"
         * willAnswer(fn) → "Call this function to compute the return value,
         * with access to the method's actual arguments"
         * **/
        @Test
        @DisplayName("Should Post Return With Updated Title And Content When Post Exists")
        void should_postReturnWithUpdatedTitleAndContent_whenPostExits() {
            // Given
            Long postId = 1L;
            Post updates = Post.builder()
                    .title("Update Post Title")
                    .content("Update Post Content")
                    .build();
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            Post result = postService.updatePostById(postId, updates);

            // Then
            assertThat(result).isNotNull();
            assertThat(result)
                    .extracting("title", "content")
                    .containsExactly("Update Post Title", "Update Post Content");

            then(postRepository).should(times(1)).findById(postId);
            then(postRepository).should(times(1)).save(any(Post.class));

        }

        @Test
        @DisplayName("Should Throw PostNotFoundException When Post Doesn't Exists")
        void should_throwPostNotFoundException_whenPostNotExists() {
            // Given
            Long postId = 999L;
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When && Then
            assertThatThrownBy(() -> postService.updatePostById(postId, samplePost))
                    .isInstanceOf(PostNotFoundException.class);

            then(postRepository).should(times(1)).findById(postId);
            then(postRepository).should(never()).save(any());
            then(postRepository).shouldHaveNoMoreInteractions();

        }
    }

    @Nested
    @DisplayName("deletePostById() Tests")
    class DeletePostTests {

         @Test
         @DisplayName("Should Delete Post When Post Exists")
         void should_deletePost_whenPostExists() {
             // Given
             Long postId = 1L;
             given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
             willDoNothing().given(postRepository).delete(samplePost);

             // When
             postService.deletePostById(postId);

             // Then
             then(postRepository).should(times(1)).findById(postId);
             then(postRepository).should(times(1)).delete(samplePost);
             then(postRepository).shouldHaveNoMoreInteractions();
         }

         @Test
         @DisplayName("Should Throw PostNotFoundException When Post Doesn't Exist")
         void should_throwPostNotFoundException_whenPostNotExists() {
             // Given
             Long postId = 1L;
             given(postRepository.findById(postId)).willReturn(Optional.empty());

             // When && Then
             assertThatThrownBy(() -> postService.deletePostById(postId))
                     .isInstanceOf(PostNotFoundException.class);

             then(postRepository).should(times(1)).findById(postId);
             then(postRepository).should(never()).delete(any(Post.class));
             then(postRepository).shouldHaveNoMoreInteractions();
         }
    }

    @Nested
    @DisplayName("getAllPosts() Tests")
    class GetAllPosts {

         @Test
         @DisplayName("Should Call Search Posts When Search Term Provided")
         void should_callSearchPosts_whenSearchTermProvided() {
             // Given
             String searchTerm = "Test Post";
             Pageable pageable = PageRequest.of(0, 10);
             Page<Post> postPage = new PageImpl<>(List.of(samplePost));
             given(postRepository.searchPosts(searchTerm, pageable)).willReturn(postPage);

             // When
             Page<Post> result = postService.getAllPosts(pageable, searchTerm);

             // Then
             assertThat(result.getContent())
                     .isNotNull()
                     .hasSize(1);

             then(postRepository).should(times(1)).searchPosts(searchTerm, pageable);
             then(postRepository).should(never()).findAll(any(Pageable.class));
             then(postRepository).shouldHaveNoMoreInteractions();
         }

        @Test
        @DisplayName("Should Call Find All When Search Term Is Null")
        void should_callFindAll_whenSearchTermIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(samplePost));
            given(postRepository.findAll(pageable)).willReturn(postPage);

            // When
            Page<Post> result = postService.getAllPosts(pageable, null);

            // Then
            assertThat(result.getContent())
                    .isNotNull()
                    .hasSize(1);

            then(postRepository).should(times(1)).findAll(pageable);
            then(postRepository).should(never()).searchPosts(any(String.class), any(Pageable.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Call Find All When Search Term Is Blank")
        void should_callFindAll_whenSearchTermIsBlank() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postPage = new PageImpl<>(List.of(samplePost));
            given(postRepository.findAll(pageable)).willReturn(postPage);

            // When
            Page<Post> result = postService.getAllPosts(pageable, " ");

            // Then
            assertThat(result.getContent())
                    .isNotNull()
                    .hasSize(1);

            then(postRepository).should(times(1)).findAll(pageable);
            then(postRepository).should(never()).searchPosts(any(String.class), any(Pageable.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

}