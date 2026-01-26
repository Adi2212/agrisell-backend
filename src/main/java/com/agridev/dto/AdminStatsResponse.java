package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to send dashboard statistics to admin frontend
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long farmers;
    private long buyers;
    private long products;
    private long orders;
    private double revenue;

}
