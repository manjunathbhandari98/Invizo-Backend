package com.quodex.Invizo.service.impl;

import com.quodex.Invizo.entity.CategoryEntity;
import com.quodex.Invizo.io.CategoryRequest;
import com.quodex.Invizo.io.CategoryResponse;
import com.quodex.Invizo.repository.CategoryRepository;
import com.quodex.Invizo.service.CategoryService;
import com.quodex.Invizo.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    // Repository to interact with the database
    private final CategoryRepository categoryRepository;

    private final FileUploadService fileUploadService;

    @Override
    public CategoryResponse addCategory(CategoryRequest request, MultipartFile file) {
        String imgUrl = fileUploadService.uploadFile(file);
        // Convert request to entity and save to DB
        CategoryEntity newCategory = convertToEntity(request);
        newCategory.setImgUrl(imgUrl);
        newCategory = categoryRepository.save(newCategory);
        // Convert saved entity to response DTO
        return convertToResponse(newCategory);
    }




    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll() // Fetch all category entities from the database
                .stream()
                .map(categoryEntity -> convertToResponse(categoryEntity)) // Convert each entity to response DTO
                .collect(Collectors.toList()); // Collect and return as a list
    }

    @Override
    public void deleteCategory(String categoryId) {
        CategoryEntity existingCategory = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
        fileUploadService.deleteFile(existingCategory.getImgUrl());
        categoryRepository.delete(existingCategory);
    }

    @Override
    public CategoryResponse getCategoryById(String categoryId) {
        CategoryEntity existingCategory = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
        return convertToResponse(existingCategory);
    }


    // Converts entity to response DTO
    private CategoryResponse convertToResponse(CategoryEntity newCategory) {
        return CategoryResponse.builder()
                .categoryId(newCategory.getCategoryId())
                .name(newCategory.getName())
                .description(newCategory.getDescription())
                .bgColor(newCategory.getBgColor())
                .imgUrl(newCategory.getImgUrl())
                .createdAt(newCategory.getCreatedAt())
                .updatedAt(newCategory.getUpdatedAt())
                .build();
    }

    // Converts request DTO to entity
    private CategoryEntity convertToEntity(CategoryRequest request) {
        return CategoryEntity.builder()
                .categoryId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .bgColor(request.getBgColor())
                .build();
    }
}
