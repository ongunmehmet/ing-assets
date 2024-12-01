package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.AuthRequest;
import com.creditmodule.ing.data.UserCustomerCreateRequest;

import com.creditmodule.ing.service.UserCustomerService;
import com.creditmodule.ing.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication and user registration")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserCustomerService userCustomerService;


    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Login", description = "Authenticate a user and return a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getAccountNumber(), authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);


        String token = jwtUtil.generateToken(authentication);

        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Register User and Customer", description = "Create a new user and associated customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User and Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUserAndCustomer(@RequestBody UserCustomerCreateRequest request) {
        String accountNumber= userCustomerService.createUserAndCustomer(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User and Customer created successfully.Please save your account number");
        response.put("accountNumber", accountNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

