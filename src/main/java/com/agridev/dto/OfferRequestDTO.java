package com.agridev.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
public class OfferRequestDTO {

    @NotBlank(message = "Offer title is required")
    @Size(min = 2, max = 150, message = "Title must be between 2 to 150 characters")
    private String title;

    @Size(max = 500, message = "Description can be up to 500 characters")
    private String description;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.01", message = "Discount must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private Double discountPercentage;

    @NotBlank(message = "Banner image URL is required")
    private String bannerUrl;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;

    @JsonProperty("active")
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @JsonProperty("isActive")
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @JsonProperty("isActive")
    public boolean getIsActive() {
        return isActive;
    }

    @JsonProperty("active")
    public boolean isActive() {
        return isActive;
    }

    private List<Long> productIds;

    // Optional list of category IDs to automatically include all products under these categories
    private List<Long> categoryIds;
}
