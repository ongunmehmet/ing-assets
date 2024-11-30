package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanResponse;
import com.creditmodule.ing.data.CustomUserDetails;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.service.LoanService;
import com.creditmodule.ing.utils.UserUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/loan")
@AllArgsConstructor
public class LoanController {
    private LoanService loanService;
    private UserUtils userUtils;
    @PostMapping("/createLoan")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public CreateLoanResponse createLoan(@RequestBody @Valid CreateLoanRequest request){
        return loanService.createLoan(request);
    }

    @GetMapping("/showLoan/{customerId}/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public Loan showLoansById(@PathVariable Long id,@PathVariable Long customerId){
        return loanService.findCustomerLoanById(id,customerId);
    }

    @GetMapping("/showLoan/{customerId}/")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.userId")
    public List<Loan> showCustomerLoans(@PathVariable Long customerId){
        return loanService.findCustomerLoansById(customerId);
    }

}
