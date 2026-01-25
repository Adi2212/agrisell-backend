package com.agridev.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripeResponse {

    private String sessionId;

    private String sessionUrl;

    private String status;

    private String message;
}
