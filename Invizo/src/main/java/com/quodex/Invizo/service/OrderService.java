package com.quodex.Invizo.service;

import com.quodex.Invizo.io.OrderRequest;
import com.quodex.Invizo.io.OrderResponse;
import com.quodex.Invizo.io.PaymentVerificationRequest;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    void deleteOrder(String orderId);

    List<OrderResponse> getLatestOrders();

    OrderResponse verifyPayment(PaymentVerificationRequest request);
}
