package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> getAllUsers(String currentEmail, String search, Pageable pageable);
    User getUserById(Long id);
    void deleteUser(Long id, String currentEmail);
}
