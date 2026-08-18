package com.borrowbox.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisputeDto {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotBlank(message = "Dispute reason is required")
    @Size(max = 100, message = "Reason cannot exceed 100 characters")
    private String reason;

    @NotBlank(message = "Dispute description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private List<String> evidenceImages;
}
