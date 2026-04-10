package com.master.Restful_Blog_Api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@EntityListeners(AuditingEntityListener.class) /// Enables Auto-Timestamping
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    /// Entity - Focus on DB Constraints

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

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
                    CascadeType.DETACH, CascadeType.MERGE,  /// When delete post, Don't delete author
                    CascadeType.PERSIST, CascadeType.REFRESH
            }
    )
    /// refers to (user_id) column in post Table
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /// refers to (post) property in Comment Class
    @OneToMany(
            mappedBy = "post",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL  /// When delete post, delete all comments
    )
    @ToString.Exclude /// prevents cycles
    private List<Comment> comments;


}
