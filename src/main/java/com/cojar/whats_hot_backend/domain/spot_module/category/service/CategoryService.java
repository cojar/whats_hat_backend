package com.cojar.whats_hot_backend.domain.spot_module.category.service;

import com.cojar.whats_hot_backend.domain.spot_module.category.entity.Category;
import com.cojar.whats_hot_backend.domain.spot_module.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(String name, Integer depth, Long parentId) {

        Category parent = this.categoryRepository.findById(parentId == null ? -1 : parentId)
                .orElse(null);

        Category category = Category.builder()
                .name(name)
                .depth(depth)
                .parent(parent)
                .build();

        this.categoryRepository.save(category);

        return category;
    }

    public Category getCategoryByName(String name) {
        return this.categoryRepository.findByName(name).orElse(null);
    }

    public Category getCategoryById(Long id) {
        if (id == null) return null;
        return this.categoryRepository.findById(id).orElse(null);
    }
}
