package com.agridev.dto;

import com.agridev.model.AccStatus;

import lombok.Data;

// DTO class to send buyer details for admin dashboard
@Data
public class BuyerAdminResponse {

    private Long id;

    private String name;

    private String email;

    private int orderCount;

    private double totalSpent;

    private AccStatus accStatus;

}
