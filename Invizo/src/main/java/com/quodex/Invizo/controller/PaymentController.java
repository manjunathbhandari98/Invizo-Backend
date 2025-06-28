package com.quodex.Invizo.controller;


import com.quodex.Invizo.io.OrderResponse;
import com.quodex.Invizo.io.PaymentRequest;
import com.quodex.Invizo.io.PaymentVerificationRequest;
import com.quodex.Invizo.io.RazorpayOrderResponse;
import com.quodex.Invizo.service.OrderService;
import com.quodex.Invizo.service.RazorpayService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    private final RazorpayService razorpayService;
    private final OrderService orderService;

    @PostMapping("/create-order")
    @ResponseStatus(HttpStatus.CREATED)
    public RazorpayOrderResponse createRazorpayOrder(@RequestBody PaymentRequest request) throws RazorpayException {
       return razorpayService.createOrder(request.getAmount(), request.getCurrency());
    }

    @PostMapping("/verify")
    public OrderResponse verifyPayment(@RequestBody PaymentVerificationRequest request){
        return orderService.verifyPayment(request);
    }
}
