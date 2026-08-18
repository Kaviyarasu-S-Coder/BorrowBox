package com.borrowbox.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondBorrowRequestDto {

    @NotNull(message = "Accept/Reject status is required")
    private Boolean accept;

    @Size(max = 500, message = "Response message cannot exceed 500 characters")
    private String responseMessage;
}
