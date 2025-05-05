package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.model.Category;
import com.inventory.inventoryservice.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "JWT")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    public static final String FAILURE = "Failure";

    private final CategoryService categoryService;
    
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        logger.info("REST request to get all Categories");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        logger.info("REST request to get Category : {}", id);
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        logger.info("REST request to get Category by name : {}", name);
        return categoryService.getCategoryByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        logger.info("REST request to save Category : {}", category);
        if (category.getId() != null) {
            return ResponseEntity.badRequest().header(FAILURE, "A new category cannot already have an ID").build();
        }
        if (categoryService.existsByName(category.getName())) {
            return ResponseEntity.badRequest().header(FAILURE, "Category name already exists").build();
        }
        Category result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        logger.info("REST request to update Category : {}, {}", id, category);
        if (category.getId() == null) {
            return ResponseEntity.badRequest().header(FAILURE, "ID cannot be null for update").build();
        }
        if (!id.equals(category.getId())) {
            return ResponseEntity.badRequest().header(FAILURE, "ID in path and body do not match").build();
        }
        
        try {
            Category result = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header(FAILURE, e.getMessage()).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("REST request to delete Category : {}", id);
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}