package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanRessponse;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.service.LoanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/loan")
public class LoanController {
    private LoanService loanService;
    @PostMapping("/createLoan")
    public CreateLoanRessponse createLoan(@RequestBody CreateLoanRequest request){

        return loanService.createLoan(request);
    }

    @GetMapping("/showLoan/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public List<Loan> showLoansById(@PathVariable Long id){
        return loanService.findLoansById(id);

    }


}
