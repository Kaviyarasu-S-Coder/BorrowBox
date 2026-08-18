package com.borrowbox.repository;

import com.borrowbox.entity.Item;
import com.borrowbox.entity.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Item> findByCategoryIdAndStatus(Long categoryId, ItemStatus status, Pageable pageable);

    Page<Item> findByStatus(ItemStatus status, Pageable pageable);

    List<Item> findTop8ByStatusOrderByCreatedAtDesc(ItemStatus status);

    List<Item> findTop8ByStatusOrderByBorrowCountDesc(ItemStatus status);

    long countByStatus(ItemStatus status);

    long countByCategoryId(Long categoryId);

    long countByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i WHERE i.status = :status AND " +
            "(LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Item> searchItems(@Param("query") String query, @Param("status") ItemStatus status, Pageable pageable);
}
