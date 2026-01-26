package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to send farmer dashboard statistics
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerStatsResponse {

    private long totalProducts;
    private long pendingOrders;
    private long completedOrders;
}
