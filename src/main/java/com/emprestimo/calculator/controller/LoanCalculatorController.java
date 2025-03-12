package com.emprestimo.calculator.controller;

import com.emprestimo.calculator.model.LoanCalculation;
import com.emprestimo.calculator.service.LoanCalculatorService;
import com.emprestimo.calculator.dto.InstallmentDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/emprestimo")
@CrossOrigin(origins = "http://localhost:3000") // Linha para React reconhecer o backend
public class LoanCalculatorController {

    private final LoanCalculatorService calculatorService;

    public LoanCalculatorController(LoanCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<List<InstallmentDTO>> calculateLoan(@Valid @RequestBody LoanCalculation loanCalculation) {
        return ResponseEntity.ok(calculatorService.calculateLoan(loanCalculation));
    }
}