// RazorpayServiceImpl.java
//
// ✅ Purpose:
// This service handles the integration with the Razorpay payment gateway.
// It is used to create payment orders on Razorpay's server, which can then be paid by the customer.
// The `createOrder` method is typically used before redirecting the customer to Razorpay checkout.
//
// ✅ What it does:
// - Reads Razorpay credentials from application properties
// - Creates a Razorpay order via API
// - Returns structured order data (as RazorpayOrderResponse)

package com.quodex.Invizo.service.impl;

import com.quodex.Invizo.io.RazorpayOrderResponse;
import com.quodex.Invizo.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service // Registers this class as a Spring-managed service bean
public class RazorpayServiceImpl implements RazorpayService {

    // Razorpay Key ID loaded from application.properties (used to authenticate with Razorpay)
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    // Razorpay Secret Key loaded from application.properties
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Creates a new order in Razorpay's system.
     * This order will be used in the frontend to initiate payment.
     *
     * @param amount   The order amount in rupees (will be multiplied by 100 as Razorpay expects paise)
     * @param currency The currency code (e.g., "INR")
     * @return RazorpayOrderResponse containing the created order details
     * @throws RazorpayException If the API call to Razorpay fails
     */
    @Override
    public RazorpayOrderResponse createOrder(Double amount, String currency) throws RazorpayException {
        // Create a Razorpay client using your API credentials
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Prepare the JSON request for Razorpay order creation
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Razorpay expects amount in paise (e.g., 100.00 INR = 10000 paise)
        orderRequest.put("currency", currency);   // Currency like "INR"
        orderRequest.put("receipt", "order_rcpid_" + System.currentTimeMillis()); // Unique receipt ID
        orderRequest.put("payment_capture", 1);   // Automatically capture payment

        // Send the order creation request to Razorpay and receive the order response
        Order order = razorpayClient.orders.create(orderRequest);

        // Convert Razorpay's Order object into your custom response DTO
        return convertToResponse(order);
    }

    /**
     * Converts Razorpay's Order object into your app's response DTO.
     *
     * @param order The Razorpay Order object
     * @return RazorpayOrderResponse with extracted fields
     */
    private RazorpayOrderResponse convertToResponse(Order order) {
        return RazorpayOrderResponse.builder()
                .id(order.get("id"))                   // Razorpay order ID
                .entity(order.get("entity"))           // Entity type (usually "order")
                .amount(order.get("amount"))           // Amount in paise
                .currency(order.get("currency"))       // Currency code (e.g., "INR")
                .status(order.get("status"))           // Order status (e.g., "created")
                .createdAt(order.get("createdAt"))     // Timestamp of order creation
                .receipt(order.get("receipt"))         // Custom receipt ID
                .build();
    }


}
