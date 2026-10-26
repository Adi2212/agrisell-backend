package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferSummaryDTO {
    private Long id;
    private String title;
    private String description;
    private Double discountPercentage;
    private String bannerUrl;
}
