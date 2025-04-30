package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.repository.CategoryRepository;
import com.inventory.inventoryservice.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryRepository.findAll();
    }
    
    @Override
    @Cacheable(value = "categories", key = "#id")
    public Optional<Category> getCategoryById(Long id) {
        logger.info("Fetching category with id: {}", id);
        return categoryRepository.findById(id);
    }
    
    @Override
    @Cacheable(value = "categories", key = "#name")
    public Optional<Category> getCategoryByName(String name) {
        logger.info("Fetching category with name: {}", name);
        return categoryRepository.findByName(name);
    }
    
    @Override
    @Transactional
    public Category createCategory(Category category) {
        logger.info("Creating new category: {}", category.getName());
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    @CachePut(value = "categories", key = "#id")
    public Category updateCategory(Long id, Category category) {
        logger.info("Updating category with id: {}", id);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Check if the new name already exists for another category
        if (!existingCategory.getName().equals(category.getName()) && 
                categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name " + category.getName() + " already exists");
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "categories", key = "#id")
    public void deleteCategory(Long id) {
        logger.info("Deleting category with id: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}