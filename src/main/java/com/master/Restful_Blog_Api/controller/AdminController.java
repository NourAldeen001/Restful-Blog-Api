package com.master.Restful_Blog_Api.controller;

import com.master.Restful_Blog_Api.dto.ApiResponse;
import com.master.Restful_Blog_Api.dto.PagedResponse;
import com.master.Restful_Blog_Api.dto.UserDTO;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.mapper.UserMapper;
import com.master.Restful_Blog_Api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;

    private final UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "createdAt") String sortBy,
                                                              @RequestParam(defaultValue = "desc") String sortDir,
                                                              @RequestParam(required = false) String search,
                                                              @AuthenticationPrincipal User currentUser) {
        log.info("Admin listing users: requestedBy={}, page={}, sizs={}, search={}",
                currentUser.getUsername(), page, size, search);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userService.getAllUsers(currentUser.getEmail(), search, pageable);

        List<UserDTO> userDTOList = userPage
                .stream()
                .map(userMapper::toUserDTO)
                .toList();

        PagedResponse<UserDTO> response = PagedResponse.<UserDTO>builder()
                .content(userDTOList)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
        log.debug("Admin users fetched: totalElements={}", userPage.getTotalElements());
        return ResponseEntity.ok(response);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long userId) {
        log.info("Admin fetching user: targetUserId={}", userId);
        UserDTO userDTO = userMapper.toUserDTO(userService.getUserById(userId));
        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long userId,
                                                       @AuthenticationPrincipal User currentUser) {
        log.info("Admin delete user request: targetUserId={}, requestedBy={}", userId, currentUser.getId());
        userService.deleteUser(userId, currentUser.getEmail());
        log.info("User deleted successfully: targetUserId={}, deletedBy={}", userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.deleted("User deleted successfully"));
    }

}
