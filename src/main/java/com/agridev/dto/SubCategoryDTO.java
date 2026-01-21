
package com.agridev.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryDTO {

    private Long id;
    private String name;
    private String imageUrl;

    // minimal parent details
    private ParentCategoryDTO parent;


}


