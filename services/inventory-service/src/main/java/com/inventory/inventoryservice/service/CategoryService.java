package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    List<Category> getAllCategories();
    
    Optional<Category> getCategoryById(Long id);
    
    Optional<Category> getCategoryByName(String name);
    
    Category createCategory(Category category);
    
    Category updateCategory(Long id, Category category);
    
    void deleteCategory(Long id);
    
    boolean existsByName(String name);
}