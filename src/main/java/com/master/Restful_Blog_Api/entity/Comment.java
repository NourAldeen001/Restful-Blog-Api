package com.master.Restful_Blog_Api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@EntityListeners(AuditingEntityListener.class) /// Enables Auto-Timestamping
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    /// Entity - Focus on DB Constraints

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.DETACH, CascadeType.MERGE,  /// When delete comment, Don't delete post
                    CascadeType.PERSIST, CascadeType.REFRESH
            }
    )
    /// refers to (post_id) column in comment Table
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.DETACH, CascadeType.MERGE,  /// When delete comment, Don't delete author
                    CascadeType.PERSIST, CascadeType.REFRESH
            }
    )
    /// refers to (user_id) column in comment Table
    @JoinColumn(name = "user_id", nullable = false)
    private User author;


}
