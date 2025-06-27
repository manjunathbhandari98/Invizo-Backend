package com.quodex.Invizo.io;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ItemResponse {
    private String itemId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imgUrl;
    private String categoryId;
    private String categoryName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
