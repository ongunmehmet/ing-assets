package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.PaymentRequest;
import com.creditmodule.ing.data.PaymentResponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.LoanInstallment;
import com.creditmodule.ing.service.LoanInstallmentService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/loanInstallments")
@AllArgsConstructor
public class LoanInstallmentController {
    private LoanInstallmentService loanInstallmentService;
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public LoanInstallment getLoan(@PathVariable Long id) {
        return loanInstallmentService.findLoanInstellmentById(id);
    }

    @GetMapping("loan/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public List<LoanInstallment> getLoanInstallments(@PathVariable Long id) {
        return loanInstallmentService.findLoanInstellmentsById(id);
    }

    @PostMapping("/payloaninstallment")
    @PreAuthorize("hasRole('ADMIN') or #payment.accountNumber == authentication.principal.username")
    public PaymentResponse payLoanInstallment (@RequestBody PaymentRequest payment){
        return loanInstallmentService.payLoanInstalments(payment);
    }
}
