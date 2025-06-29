package com.quodex.Invizo.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorpayOrderResponse {
    private String id;
    private String entity;
    private String currency;
    private Integer amount;
    private String status;
    private Date createdAt;
    private String receipt;
}
