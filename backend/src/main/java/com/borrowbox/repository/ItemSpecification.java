package com.borrowbox.repository;

import com.borrowbox.dto.request.ItemFilterCriteria;
import com.borrowbox.entity.Item;
import com.borrowbox.entity.ItemStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemSpecification {

    public static Specification<Item> withFilters(ItemFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Only AVAILABLE items in public searches
            predicates.add(cb.equal(root.get("status"), ItemStatus.AVAILABLE));

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 2. Text Search Query (Title, Description, SubCategory, Location)
            if (StringUtils.hasText(criteria.getQuery())) {
                String searchPattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate subCatMatch = cb.like(cb.lower(root.get("subCategory")), searchPattern);
                Predicate locMatch = cb.like(cb.lower(root.get("location")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch, subCatMatch, locMatch));
            }

            // 3. Category Filter
            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
            } else if (StringUtils.hasText(criteria.getCategorySlug())) {
                predicates.add(cb.equal(cb.lower(root.get("category").get("slug")), criteria.getCategorySlug().trim().toLowerCase()));
            }

            // 4. Conditions
            if (criteria.getConditions() != null && !criteria.getConditions().isEmpty()) {
                predicates.add(root.get("condition").in(criteria.getConditions()));
            }

            // 5. Deposit Range
            if (criteria.getMinDeposit() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("depositAmount"), criteria.getMinDeposit()));
            }
            if (criteria.getMaxDeposit() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("depositAmount"), criteria.getMaxDeposit()));
            }

            // 6. Max Daily Rate
            if (criteria.getMaxDailyRate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dailyRate"), criteria.getMaxDailyRate()));
            }

            // 7. Lending Mode
            if (StringUtils.hasText(criteria.getLendingMode())) {
                predicates.add(cb.equal(root.get("lendingMode"), criteria.getLendingMode()));
            }

            // 8. Locality Match
            if (StringUtils.hasText(criteria.getLocation())) {
                String locPattern = "%" + criteria.getLocation().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
