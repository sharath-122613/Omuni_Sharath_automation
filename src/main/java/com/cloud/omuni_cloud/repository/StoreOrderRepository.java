package com.cloud.omuni_cloud.repository;

import com.cloud.omuni_cloud.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    Optional<StoreOrder> findFirstByOrderIdOrderByIdDesc(String orderId);
}
