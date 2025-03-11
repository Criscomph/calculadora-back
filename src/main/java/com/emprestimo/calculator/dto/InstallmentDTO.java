package com.emprestimo.calculator.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class InstallmentDTO {
    private LocalDate dataCompetencia;
    private Double valorEmprestimo;
    private Double saldoDevedor;
    private Double parcela;
    private Double total;
    private Double amortizacao;
    private Double saldo;
    private Double provisao;
    private Double acumulado;
    private Boolean pago;
} 