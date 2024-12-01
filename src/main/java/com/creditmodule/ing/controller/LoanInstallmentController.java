package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.PaymentRequest;
import com.creditmodule.ing.data.PaymentResponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.LoanInstallment;
import com.creditmodule.ing.service.LoanInstallmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Loan Installments", description = "Endpoints for loan installment management")
public class LoanInstallmentController {
    private LoanInstallmentService loanInstallmentService;

    @Operation(summary = "Get Loan Installment by ID", description = "Retrieve loan installment details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan installment found"),
            @ApiResponse(responseCode = "404", description = "Loan installment not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<LoanInstallment> getLoan(@PathVariable Long id) {
        LoanInstallment loanInstallment = loanInstallmentService.findLoanInstellmentById(id);
        return ResponseEntity.ok(loanInstallment);
    }

    @Operation(summary = "Get All Loan Installments", description = "Retrieve all installments for a specific loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan installments retrieved"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @GetMapping("loan/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<List<LoanInstallment>> getLoanInstallments(@PathVariable Long id) {
        List<LoanInstallment> installments = loanInstallmentService.findLoanInstellmentsById(id);
        return ResponseEntity.ok(installments);
    }

    @Operation(summary = "Pay Loan Installment", description = "Make a payment for a loan installment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successful"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request")
    })
    @PostMapping("/payloaninstallment")
    @PreAuthorize("hasRole('ADMIN') or #payment.accountNumber == authentication.principal.username")
    public ResponseEntity<PaymentResponse> payLoanInstallment(@RequestBody PaymentRequest payment) {
        PaymentResponse response = loanInstallmentService.payLoanInstalments(payment);
        return ResponseEntity.ok(response);
    }
}
