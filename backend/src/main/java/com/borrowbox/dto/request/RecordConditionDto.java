package com.borrowbox.dto.request;

import com.borrowbox.entity.ConditionStage;
import com.borrowbox.entity.ItemCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordConditionDto {

    @NotNull(message = "Condition stage (PICKUP or RETURN) is required")
    private ConditionStage stage;

    @NotNull(message = "Observed condition rating is required")
    private ItemCondition condition;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();
}
