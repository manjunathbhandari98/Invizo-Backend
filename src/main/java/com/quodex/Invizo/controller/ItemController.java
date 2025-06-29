package com.quodex.Invizo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodex.Invizo.io.ItemRequest;
import com.quodex.Invizo.io.ItemResponse;
import com.quodex.Invizo.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;


    @PostMapping("/admin/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse addItem(@RequestPart("item")String itemString, @RequestPart("file")MultipartFile file){
        // Create an ObjectMapper to parse JSON string into Java object
        ObjectMapper objectMapper = new ObjectMapper();
        ItemRequest request = null;

        try {
            // Convert JSON string to ItemRequest object
            request = objectMapper.readValue(itemString, ItemRequest.class);

            // Call the service to save item along with the image file
            return itemService.addItem(request, file);

        } catch (JsonProcessingException e) {
            // If JSON parsing fails, throw a 400 Bad Request error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/items")
    public List<ItemResponse> getItems(){
        return itemService.getItems();
    }

    @GetMapping("items/{itemId}")
    public ItemResponse getItemById(@PathVariable String itemId){
        return itemService.getItemById(itemId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/item/{itemId}")
    public void deleteItemById(@PathVariable String itemId){
        try {
            itemService.deleteItem(itemId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Category Not Found");
        }
    }
}
