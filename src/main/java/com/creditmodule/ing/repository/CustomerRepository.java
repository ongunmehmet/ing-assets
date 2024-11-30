package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findById(Long id);
}
