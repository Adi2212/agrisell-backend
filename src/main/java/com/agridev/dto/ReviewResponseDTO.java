package com.agridev.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDTO {

    private Long id;
    private int rating;
    private String comment;

    private String buyerName;

    private LocalDateTime createdAt;
}
