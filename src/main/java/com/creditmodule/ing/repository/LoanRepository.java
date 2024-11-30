package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository  extends JpaRepository<Loan,Long> {
    @Query("SELECT u FROM Loan u WHERE u.customer.id= :id")
    List<Loan> findByCustomerId(@Param("id") Long customerId);
    @Query("SELECT u FROM Loan u WHERE u.customer.id= :customerId and u.id = :id")
    Loan findByCustomerIdAndLoanId(@Param("id") Long id, @Param("customerId")Long customerId);
}
