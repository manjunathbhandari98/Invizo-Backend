package com.quodex.Invizo.repository;

import com.quodex.Invizo.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

}
