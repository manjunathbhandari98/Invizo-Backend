package com.quodex.Invizo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String itemId;
   private String name;
   private String description;
   private BigDecimal price;
   @CreationTimestamp
   @Column(updatable = false)
   private Timestamp createdAt;
   @UpdateTimestamp
   private Timestamp updatedAt;
   private String imgUrl;
   @ManyToOne()
   @JoinColumn(name = "category_id", nullable = false)
   @OnDelete(action = OnDeleteAction.RESTRICT)
   private CategoryEntity category;
}
