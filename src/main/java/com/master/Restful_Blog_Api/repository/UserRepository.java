package com.master.Restful_Blog_Api.repository;

import com.master.Restful_Blog_Api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
            "u.email != :email AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllExceptEmail(@Param("email") String currentEmail,
                                  @Param("search") String search,
                                  Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.email != :email")
    Page<User> findAllExceptEmail(@Param("email") String currentEmail,
                                  Pageable pageable);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
