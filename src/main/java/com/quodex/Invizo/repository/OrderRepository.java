// This interface acts as a Data Access Layer for OrderEntity and extends JpaRepository
// JpaRepository provides standard CRUD operations and more
// OrderEntity - the type of entity, Long - the type of primary key

package com.quodex.Invizo.repository;

import com.quodex.Invizo.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Finds a single order using its unique orderId (not the primary key 'id')
    // Returns Optional to handle cases where the orderId might not exist
    Optional<OrderEntity> findByOrderId(String orderId);

    // Retrieves all orders sorted by 'createdAt' in descending order
    // Most recent orders will be at the top
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    // Custom query using JPQL to calculate the total sales (sum of grandTotal) for a specific date
    // DATE() function extracts the date from 'createdAt' timestamp
    @Query("SELECT SUM(o.grandTotal) FROM OrderEntity o WHERE DATE(o.createdAt) = :date")
    Double sumSalesByDate(@Param("date") LocalDate date);

    // Custom query to count the number of orders placed on a specific date
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE DATE(o.createdAt) = :date")
    Long countByOrderDate(@Param("date") LocalDate date);

    @Query(value = "SELECT * FROM orders ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<OrderEntity> findTop5RecentOrdersNative();

}
