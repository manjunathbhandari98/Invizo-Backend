package com.quodex.Invizo.service.impl;

import com.quodex.Invizo.entity.CategoryEntity;
import com.quodex.Invizo.entity.ItemEntity;
import com.quodex.Invizo.io.ItemRequest;
import com.quodex.Invizo.io.ItemResponse;
import com.quodex.Invizo.repository.CategoryRepository;
import com.quodex.Invizo.repository.ItemRepository;
import com.quodex.Invizo.service.FileUploadService;
import com.quodex.Invizo.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    
    @Override
    public ItemResponse addItem(ItemRequest request, MultipartFile file) {
        String imgUrl = fileUploadService.uploadFile(file);
        // Convert request to entity and save to DB
        ItemEntity newItem = convertToEntity(request);
        CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
        newItem.setCategory(category);
        newItem.setImgUrl(imgUrl);
        newItem = itemRepository.save(newItem);
        // Convert saved entity to response DTO
        return convertToResponse(newItem);
    }


    @Override
    public List<ItemResponse> getItems() {
        return itemRepository.findAll()
                .stream().map(itemEntity -> convertToResponse(itemEntity))
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponse getItemById(String itemId) {
        ItemEntity item = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));
        return convertToResponse(item);
    }

    @Override
    public void deleteItem(String itemId) {
        ItemEntity item = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item Not Found"));
        boolean isFileDeleted = fileUploadService.deleteFile(item.getImgUrl());
        if (isFileDeleted) {
            itemRepository.delete(item);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to Delete the Item");
        }

    }


    private ItemResponse convertToResponse(ItemEntity newItem) {

        return ItemResponse.builder()
                .itemId(newItem.getItemId())
                .name(newItem.getName())
                .description(newItem.getDescription())
                .price(newItem.getPrice())
                .categoryId(newItem.getCategory().getCategoryId())
                .categoryName(newItem.getCategory().getName())

                .imgUrl(newItem.getImgUrl())
                .createdAt(newItem.getCreatedAt())
                .updatedAt(newItem.getUpdatedAt())
                .build();
    }

    private ItemEntity convertToEntity(ItemRequest request) {
        return ItemEntity.builder()
                .itemId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
    }
}
