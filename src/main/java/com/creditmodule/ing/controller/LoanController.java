package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanResponse;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/loan")
@AllArgsConstructor
@Tag(name = "Loan", description = "Endpoints for loan management")
public class LoanController {
    private LoanService loanService;

    @Operation(summary = "Create Loan", description = "Create a new loan for a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid loan request")
    })
    @PostMapping("/createLoan")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public ResponseEntity<CreateLoanResponse> createLoan(@RequestBody @Valid CreateLoanRequest request) {
        CreateLoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Show Loan by ID", description = "Retrieve a specific loan by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan details retrieved"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @GetMapping("/showLoan/{customerId}/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<Loan> showLoansById(@PathVariable Long id, @PathVariable Long customerId) {
        Loan loan = loanService.findCustomerLoanById(id, customerId);
        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "Show Customer Loans", description = "Retrieve all loans for a specific customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/showLoan/{customerId}/")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.userId")
    public ResponseEntity<List<Loan>> showCustomerLoans(@PathVariable Long customerId) {
        List<Loan> loans = loanService.findCustomerLoansById(customerId);
        return ResponseEntity.ok(loans);
    }

}
