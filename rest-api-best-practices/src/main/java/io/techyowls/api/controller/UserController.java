package io.techyowls.api.controller;

import io.techyowls.api.dto.*;
import io.techyowls.api.exception.ResourceNotFoundException;
import io.techyowls.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * REST API following best practices:
 * - Plural nouns for resources
 * - HTTP methods for actions
 * - Proper status codes
 * - Pagination for lists
 * - DTOs for request/response
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/v1/users - List all (paginated)
    @GetMapping
    public Page<UserSummary> listUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    // GET /api/v1/users/123 - Get one
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // POST /api/v1/users - Create
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/v1/users/123 - Full update
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    // PATCH /api/v1/users/123 - Partial update
    @PatchMapping("/{id}")
    public UserResponse patchUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return userService.patch(id, updates);
    }

    // DELETE /api/v1/users/123 - Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
