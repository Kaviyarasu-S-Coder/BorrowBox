package com.borrowbox.dto.response;

import com.borrowbox.entity.ConditionStage;
import com.borrowbox.entity.ItemCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionConditionResponse {

    private Long id;
    private ConditionStage stage;
    private ItemCondition condition;
    private String notes;
    private Long recordedById;
    private String recordedByName;
    private List<String> photos;
    private LocalDateTime createdAt;
}
