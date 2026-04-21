package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.ForbiddenException;
import com.master.Restful_Blog_Api.exception.UserNotFoundException;
import com.master.Restful_Blog_Api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Page<User> getAllUsers(String currentEmail, String search, Pageable pageable) {
        if(search != null && !search.isBlank()) {
            return userRepository.findAllExceptEmail(currentEmail, search, pageable);
        }
        else {
            return userRepository.findAllExceptEmail(currentEmail, pageable);
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public void deleteUser(Long id, String currentUserEmail) {
        User user = getUserById(id);

        if(user.getEmail().equals(currentUserEmail)) {
            throw new ForbiddenException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }
}
