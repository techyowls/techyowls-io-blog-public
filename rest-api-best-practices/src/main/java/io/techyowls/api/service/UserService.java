package io.techyowls.api.service;

import io.techyowls.api.dto.*;
import io.techyowls.api.exception.ResourceNotFoundException;
import io.techyowls.api.model.User;
import io.techyowls.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Page<UserSummary> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(UserSummary::from);
    }

    public Optional<UserResponse> findById(Long id) {
        return repository.findById(id).map(UserResponse::from);
    }

    public UserResponse create(CreateUserRequest request) {
        User user = new User(request.name(), request.email(), request.age());
        return UserResponse.from(repository.save(user));
    }

    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());

        return UserResponse.from(repository.save(user));
    }

    public UserResponse patch(Long id, Map<String, Object> updates) {
        User user = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("age")) {
            user.setAge((Integer) updates.get("age"));
        }

        return UserResponse.from(repository.save(user));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        repository.deleteById(id);
    }
}
