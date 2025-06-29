package com.quodex.Invizo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodex.Invizo.io.CategoryRequest;
import com.quodex.Invizo.io.CategoryResponse;
import com.quodex.Invizo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API endpoint to add a new category along with an image file.
     *
     * Accepts multipart/form-data with:
     * - A JSON string for category details (`category`)
     * - A file for the category image (`file`)
     *
     * @param categoryString JSON string representing CategoryRequest
     * @param file Image file (e.g., PNG, JPG)
     * @return The saved CategoryResponse with details
     */
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse addCategory(
            @RequestPart("category") String categoryString, // Category data as JSON string
            @RequestPart("file") MultipartFile file         // File upload (image)
    ) {
        // Create an ObjectMapper to parse JSON string into Java object
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryRequest request = null;

        try {
            // Convert JSON string to CategoryRequest object
            request = objectMapper.readValue(categoryString, CategoryRequest.class);

            // Call the service to save category along with the image file
            return categoryService.addCategory(request, file);

        } catch (JsonProcessingException e) {
            // If JSON parsing fails, throw a 400 Bad Request error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping("/categories")
    public List<CategoryResponse> getCategories(){
        return categoryService.getCategories();
    }

    @GetMapping("/admin/categories/{categoryId}")
    public CategoryResponse getCategoryById(@PathVariable String categoryId){
        return categoryService.getCategoryById(categoryId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{categoryId}")
    public void deleteCategoryById(@PathVariable String categoryId){
        try {
            categoryService.deleteCategory(categoryId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Category Not Found");
        }
    }
}
