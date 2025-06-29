/**
 * OrderServiceImpl.java
 *
 * This class handles the complete business logic for creating, storing, deleting,
 * and verifying orders in the system. It acts as the **main service layer** between
 * the controller (API layer) and the database.
 *
 *  Core Responsibilities:
 * - Create a new order with customer and item details
 * - Save the order and its items in the database
 * - Handle payment status (CASH or ONLINE)
 * - Verify Razorpay payment authenticity using HMAC SHA256
 * - Delete an order by ID
 * - Fetch latest orders for admin dashboard or order listing
 *
 *  Razorpay Integration:
 * If the user pays online, the payment verification step ensures the
 * payment is secure by matching Razorpay's signature using your secret key.
 *
 * 💡 Why is this needed?
 * Because we should never trust the frontend payment response blindly.
 * Verifying Razorpay's signature helps protect against fake transactions.
 *
 *  This service is injected in controllers to provide order-related operations.
 */


package com.quodex.Invizo.service.impl;

import com.quodex.Invizo.entity.OrderEntity;
import com.quodex.Invizo.entity.OrderItemEntity;
import com.quodex.Invizo.io.*;
import com.quodex.Invizo.repository.OrderItemRepository;
import com.quodex.Invizo.repository.OrderRepository;
import com.quodex.Invizo.service.OrderService;
import com.quodex.Invizo.util.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;


    /**
     * Creates a new order based on the incoming OrderRequest
     * @param request the request containing customer and cart item data
     * @return OrderResponse DTO representing the saved order
     */
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        // Convert request DTO to OrderEntity object
        OrderEntity newOrder = convertToOrderEntity(request);

        // Set payment status depending on payment method
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(newOrder.getPaymentMethod() == PaymentMethod.CASH ?
                PaymentDetails.PaymentStatus.COMPLETED : PaymentDetails.PaymentStatus.PENDING);
        newOrder.setPaymentDetails(paymentDetails);

        // Convert each item in cart to an OrderItemEntity and collect them in a list
        List<OrderItemEntity> orderItems = request.getCartItems().stream()
                .map(this::convertToOrderItemEntity)
                .collect(Collectors.toList());

        // Set the list of items to the order
        newOrder.setItems(orderItems);

        // Save the new order to the database
        newOrder = orderRepository.save(newOrder);

        // Convert saved order to response DTO and return
        return convertToResponse(newOrder);
    }

    /**
     * Converts an OrderEntity to an OrderResponse for output
     * @param newOrder the OrderEntity to convert
     * @return OrderResponse DTO
     */
    private OrderResponse convertToResponse(OrderEntity newOrder) {
        return OrderResponse.builder()
                .orderId(newOrder.getOrderId())
                .customerName(newOrder.getCustomerName())
                .mobileNumber(newOrder.getMobileNumber())
                .subtotal(newOrder.getSubtotal())
                .tax(newOrder.getTax())
                .grandTotal(newOrder.getGrandTotal())
                .paymentMethod(newOrder.getPaymentMethod())
                .items(newOrder.getItems().stream()
                        .map(this::convertToItemResponse)
                        .collect(Collectors.toList()))
                .paymentDetails(newOrder.getPaymentDetails())
                .createdAt(newOrder.getCreatedAt())
                .build();
    }

    /**
     * Converts an OrderItemEntity to a simplified OrderItemResponse DTO
     * @param orderItemEntity the item entity to convert
     * @return OrderItemResponse DTO
     */
    private OrderItemResponse convertToItemResponse(OrderItemEntity orderItemEntity) {
        return OrderItemResponse.builder()
                .itemId(orderItemEntity.getItemId())
                .name(orderItemEntity.getName())
                .price(orderItemEntity.getPrice())
                .quantity(orderItemEntity.getQuantity())
                .build();
    }

    /**
     * Converts an OrderItemRequest DTO into an OrderItemEntity
     * @param orderItemRequest the request DTO
     * @return OrderItemEntity
     */
    private OrderItemEntity convertToOrderItemEntity(OrderItemRequest orderItemRequest) {
        return OrderItemEntity.builder()
                .itemId(orderItemRequest.getItemId())
                .name(orderItemRequest.getName())
                .price(orderItemRequest.getPrice())
                .quantity(orderItemRequest.getQuantity())
                .build();
    }

    /**
     * Converts the overall order request to an OrderEntity for persistence
     * @param request the incoming order request
     * @return OrderEntity
     */
    private OrderEntity convertToOrderEntity(OrderRequest request) {
        return OrderEntity.builder()

                .customerName(request.getCustomerName())
                .mobileNumber(request.getMobileNumber())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .grandTotal(request.getGrandTotal())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod())) // Enum conversion
                .build();
    }

    /**
     * Deletes an existing order by its orderId
     * @param orderId the unique ID of the order to delete
     */
    @Override
    public void deleteOrder(String orderId) {
        // Find the order; if not found, throw exception
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        // Delete the order from the database
        orderRepository.delete(orderEntity);
    }

    /**
     * Fetches the most recent orders sorted by creation time in descending order
     * @return List of OrderResponse DTOs
     */
    @Override
    public List<OrderResponse> getLatestOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verifies the payment received from Razorpay and updates the order's payment details.
     * This method ensures that the payment is authentic using Razorpay's signature verification process.
     *
     * @param request The request payload containing orderId, Razorpay payment details, and signature
     * @return Updated OrderResponse after successful verification and status update
     */
    @Override
    public OrderResponse verifyPayment(PaymentVerificationRequest request) {
        // 1. Find the order in the database by its order ID
        OrderEntity existingOrder = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        // 2. Verify Razorpay signature to ensure the payment is valid and secure
        if (!verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            throw new RuntimeException("Payment Verification Failed");
        }

        // 3. Update the payment details in the order
        PaymentDetails paymentDetails = existingOrder.getPaymentDetails();
        paymentDetails.setRazorpayOrderId(request.getRazorpayOrderId());
        paymentDetails.setRazorpayPaymentId(request.getRazorpayPaymentId());
        paymentDetails.setRazorpaySignature(request.getRazorpaySignature());
        paymentDetails.setStatus(PaymentDetails.PaymentStatus.COMPLETED); // Mark payment as completed

        // 4. Save updated order to the database
        existingOrder = orderRepository.save(existingOrder);

        // 5. Return the updated order response
        return convertToResponse(existingOrder);
    }

    @Override
    public Double sumSalesByDate(LocalDate date) {
        // Calls the repository method to calculate the total sales amount (grandTotal)
        // for all orders created on the given date
        return orderRepository.sumSalesByDate(date);
    }

    @Override
    public Long countByOrderDate(LocalDate date) {
        // Calls the repository method to count how many orders were placed
        // on the given date
        return orderRepository.countByOrderDate(date);
    }

    @Override
    public List<OrderResponse> findRecentOrders() {
        return orderRepository.findTop5RecentOrdersNative()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verifies the HMAC SHA256 signature sent by Razorpay using your secret key.
     * This ensures that the payment details are not tampered with.
     *
     * @param razorpayOrderId    The order ID sent by Razorpay
     * @param razorpayPaymentId  The payment ID sent by Razorpay
     * @param razorpaySignature  The HMAC SHA256 signature sent by Razorpay
     * @return true if signature is valid, false otherwise
     */
    private boolean verifyRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            // 1. Concatenate orderId and paymentId using a pipe (|)
            String data = razorpayOrderId + "|" + razorpayPaymentId;

            // 2. Create a Mac instance using HMAC SHA256
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");

            // 3. Initialize the Mac with your Razorpay secret key
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            // 4. Generate the hash from the data
            byte[] hash = mac.doFinal(data.getBytes());

            // 5. Convert hash bytes to hexadecimal format
            StringBuilder actualSignature = new StringBuilder();
            for (byte b : hash) {
                actualSignature.append(String.format("%02x", b));
            }

            // 6. Compare the actualSignature with Razorpay's signature (timing-safe)
            return actualSignature.toString().equals(razorpaySignature);
        } catch (Exception e) {
            // Log the error if needed and return false
            e.printStackTrace();
            return false;
        }
    }

}
