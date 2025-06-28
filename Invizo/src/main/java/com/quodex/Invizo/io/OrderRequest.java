package com.quodex.Invizo.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String customerName;
    private String mobileNumber;
    private List<OrderItemRequest> cartItems;
    private Double subtotal;
    private Double tax;
    private Double grandTotal;
    private String paymentMethod;

}
