package com.agridev.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDTO {
    private Long id;
    private String title;
    private String description;
    private Double discountPercentage;
    private String bannerUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("active")
    public boolean getActive() {
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    private int productCount;
    private List<Long> productIds;
    private List<ProductSummaryDTO> products;
}
