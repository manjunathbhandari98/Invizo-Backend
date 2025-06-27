package com.quodex.Invizo.service;

import com.quodex.Invizo.io.ItemRequest;
import com.quodex.Invizo.io.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    ItemResponse addItem(ItemRequest request, MultipartFile file);

    List<ItemResponse> getItems();

    ItemResponse getItemById(String itemId);

    void deleteItem(String itemId);
}
