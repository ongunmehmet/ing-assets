package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, Date startDate, Date endDate);
    List<Order> findByStatus(Status status);
}
