package com.borrowbox.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBorrowRequestDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Please describe the intended purpose or project")
    @Size(min = 5, max = 500, message = "Purpose must be between 5 and 500 characters")
    private String purpose;

    @NotBlank(message = "Message to item owner is required")
    @Size(min = 5, max = 1000, message = "Message must be between 5 and 1000 characters")
    private String message;
}
