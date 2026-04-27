package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.CategoryRepository;
import com.example.productcrud.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        return categoryRepository.findByUserIdOrderByNameAsc(user.getId());
    }

    public Optional<Category> findByIdAndUsername(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        return categoryRepository.findByIdAndUserId(id, user.getId());
    }

    public Category save(Category category, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        category.setUserId(user.getId());
        return categoryRepository.save(category);
    }

    public void deleteByIdAndUsername(Long id, String username) {
        Optional<Category> category = findByIdAndUsername(id, username);
        category.ifPresent(categoryRepository::delete);
    }

    public boolean existsByNameAndUsername(String name, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        return categoryRepository.existsByNameAndUserId(name, user.getId());
    }

    public boolean existsByNameAndUsernameExcludingId(String name, String username, Long excludeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        Optional<Category> existing = categoryRepository.findByIdAndUserId(excludeId, user.getId());
        if (existing.isPresent() && existing.get().getName().equals(name)) {
            return false;
        }
        return categoryRepository.existsByNameAndUserId(name, user.getId());
    }
}