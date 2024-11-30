package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    @Query("SELECT u FROM LoanInstallment u WHERE u.loan.id = :loanId")
    List<LoanInstallment> findAllByLoanId(@Param("loanId") Long loanId);
}
