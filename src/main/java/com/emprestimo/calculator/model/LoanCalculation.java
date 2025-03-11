package com.emprestimo.calculator.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class LoanCalculation {
    @NotNull(message = "Data inicial é obrigatória")
    private LocalDate dataInicial;
    
    @NotNull(message = "Data final é obrigatória")
    private LocalDate dataFinal;
    
    @NotNull(message = "Data do primeiro pagamento é obrigatória")
    private LocalDate primeiroPagamento;
    
    @NotNull(message = "Valor do empréstimo é obrigatório")
    @Positive(message = "Valor do empréstimo deve ser positivo")
    private Double valorEmprestimo;
    
    @NotNull(message = "Taxa de juros é obrigatória")
    @Positive(message = "Taxa de juros deve ser positiva")
    private Double taxaJuros;
} 