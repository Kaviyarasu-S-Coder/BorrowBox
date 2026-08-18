package com.borrowbox.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusResponse {

    private Long itemId;

    @JsonProperty("isFavorited")
    private boolean isFavorited;

    private long totalFavorites;
    private String message;
}
