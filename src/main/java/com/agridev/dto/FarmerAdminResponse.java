package com.agridev.dto;

import com.agridev.model.AccStatus;

import lombok.Data;

// DTO class to send farmer details for admin dashboard
@Data
public class FarmerAdminResponse {

    private Long id;

    private String name;

    private String phone;

    private int productCount;

    private AccStatus accStatus;

}
