package com.cloud.omuni_cloud.repository;

import com.cloud.omuni_cloud.entity.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {
    Optional<SaleOrder> findFirstByOrderNoOrderByIdDesc(String orderNo);
}
