package com.agridev.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double discountPercentage;

    @Column(nullable = false, length = 1000)
    private String bannerUrl;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;

    @OneToMany(mappedBy = "offer")
    @JsonIgnore
    @Builder.Default
    private List<Product> products = new ArrayList<>();

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
}
