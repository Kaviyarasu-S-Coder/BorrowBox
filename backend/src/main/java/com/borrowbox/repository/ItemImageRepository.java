package com.borrowbox.repository;

import com.borrowbox.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findByItemIdOrderByDisplayOrderAsc(Long itemId);

    void deleteByItemId(Long itemId);
}
