package com.quodex.Invizo.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    private String itemId;
    private String name;
    private Double price;
    private Integer quantity;
}
