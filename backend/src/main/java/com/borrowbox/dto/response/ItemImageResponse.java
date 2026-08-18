package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemImageResponse {

    private Long id;
    private String imageUrl;
    private boolean isPrimary;
    private Integer displayOrder;
}
