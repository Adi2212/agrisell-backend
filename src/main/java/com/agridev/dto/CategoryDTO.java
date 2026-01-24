
package com.agridev.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private String imgUrl;

    // To avoid circular references, only pass minimal parent details
    private ParentCategoryDTO parent;

    private List<SubCategoryDTO> subcategories;


}
