package com.quodex.Invizo.service;

import com.quodex.Invizo.io.CategoryRequest;
import com.quodex.Invizo.io.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    CategoryResponse addCategory(CategoryRequest request, MultipartFile file);

    List<CategoryResponse> getCategories();

    void deleteCategory(String categoryId);

    CategoryResponse getCategoryById(String categoryId);
}
