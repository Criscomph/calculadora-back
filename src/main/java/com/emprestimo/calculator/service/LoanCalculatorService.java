package com.emprestimo.calculator.service;

import com.emprestimo.calculator.model.LoanCalculation;
import com.emprestimo.calculator.dto.InstallmentDTO;
import com.emprestimo.calculator.util.LoanConstants;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoanCalculatorService {

    private static final int NUMERO_PARCELAS = 120;
    private static final int DIAS_BASE = 360;

    public List<InstallmentDTO> calculateLoan(LoanCalculation loan) {
        validateDates(loan);
        List<InstallmentDTO> installments = new ArrayList<>();
        
        double valorEmprestimo = loan.getValorEmprestimo();
        double taxaAnual = loan.getTaxaJuros() / 100.0;
        double amortizacaoMensal = round(valorEmprestimo / NUMERO_PARCELAS);
        
        // Registro inicial
        InstallmentDTO inicial = createInstallment(
            loan.getDataInicial(),
            valorEmprestimo,
            valorEmprestimo,
            "",
            0.0,
            0.0,
            valorEmprestimo,
            0.0,
            0.0,
            0.0  
        );
        installments.add(inicial);
        
        LocalDate dataAtual = loan.getDataInicial();
        double saldoDevedorBase = valorEmprestimo;
        double saldoDevedorAtual = valorEmprestimo;
        double jurosAcumulado = 0.0;
        int numeroParcela = 1;
        
        while (!dataAtual.isAfter(loan.getDataFinal())) {
            LocalDate proximaData;
            
            // Determina a próxima data
            if (dataAtual.equals(loan.getDataInicial())) {
                proximaData = dataAtual.withDayOfMonth(dataAtual.lengthOfMonth());
            } else if (dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth()) {
                // Se estamos no fim do mês, próxima data é dia 15-18 ou fim do próximo mês
                LocalDate proximoPagamento = getProximaDataPagamento(dataAtual.plusMonths(1), numeroParcela);
                if (proximoPagamento.isAfter(loan.getPrimeiroPagamento()) || 
                    proximoPagamento.equals(loan.getPrimeiroPagamento())) {
                    proximaData = proximoPagamento;
                } else {
                    proximaData = dataAtual.plusMonths(1).withDayOfMonth(
                        dataAtual.plusMonths(1).lengthOfMonth());
                }
            } else {
                proximaData = dataAtual.withDayOfMonth(dataAtual.lengthOfMonth());
            }
            
            long diasPeriodo = ChronoUnit.DAYS.between(dataAtual, proximaData);
            double fatorJuros = Math.pow(1 + taxaAnual, diasPeriodo / (double)DIAS_BASE) - 1;
            double jurosPeriodo = round(saldoDevedorBase * fatorJuros);
            
            boolean isPagamento = proximaData.getDayOfMonth() >= 15 && proximaData.getDayOfMonth() <= 18 && 
                (proximaData.isAfter(loan.getPrimeiroPagamento()) || 
                proximaData.equals(loan.getPrimeiroPagamento()));
            
            InstallmentDTO installment;
            if (isPagamento) {
                jurosAcumulado += jurosPeriodo;
                String consolidada = String.format("%d/%d", numeroParcela, NUMERO_PARCELAS);
                
                saldoDevedorBase = round(saldoDevedorBase - amortizacaoMensal);
                double valorParcela = round(amortizacaoMensal + jurosAcumulado);
                
                installment = createInstallment(
                    proximaData,
                    0.0,
                    saldoDevedorBase,
                    consolidada,
                    valorParcela,
                    amortizacaoMensal,
                    saldoDevedorBase,
                    jurosPeriodo,
                    0.0,
                    jurosAcumulado  // Pago recebe o valor dos juros acumulados
                );
                
                numeroParcela++;
                jurosAcumulado = 0.0;
                saldoDevedorAtual = saldoDevedorBase;
            } else {
                saldoDevedorAtual = round(saldoDevedorBase + jurosPeriodo);
                jurosAcumulado += jurosPeriodo;
                
                installment = createInstallment(
                    proximaData,
                    0.0,
                    saldoDevedorAtual,
                    "",
                    0.0,
                    0.0,
                    saldoDevedorBase,
                    jurosPeriodo,
                    jurosAcumulado,
                    0.0  // Pago como double
                );
            }
            
            installments.add(installment);
            dataAtual = proximaData;
        }
        
        return installments;
    }
    
    private LocalDate getProximaDataPagamento(LocalDate data, int numeroParcela) {
        int dia;
        int mes = data.getMonthValue();
        
        // Define o dia do pagamento com base no mês
        if (mes == 6) { // Junho
            dia = 17;
        } else if (mes == 9) { // Setembro
            dia = 16;
        } else if (mes == 11) { // Novembro
            dia = 18;
        } else if (mes == 12) { // Dezembro
            dia = 16;
        } else {
            dia = 15;
        }
        
        return data.withDayOfMonth(dia);
    }
    
    private InstallmentDTO createInstallment(
            LocalDate dataCompetencia,
            double valorEmprestimo,
            double saldoDevedor,
            String consolidada,
            double parcela,
            double amortizacao,
            double saldo,
            double provisao,
            double acumulado,
            double pago) {  // Mudado para double
        InstallmentDTO installment = new InstallmentDTO();
        installment.setDataCompetencia(dataCompetencia);
        installment.setValorEmprestimo(round(valorEmprestimo));
        installment.setSaldoDevedor(round(saldoDevedor));
        installment.setConsolidada(consolidada);
        installment.setParcela(round(parcela));
        installment.setTotal(round(parcela));
        installment.setAmortizacao(round(amortizacao));
        installment.setSaldo(round(saldo));
        installment.setProvisao(round(provisao));
        installment.setAcumulado(round(acumulado));
        installment.setPago(round(pago));  // Arredonda o valor pago
        return installment;
    }
    
    private double round(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private void validateDates(LoanCalculation loan) {
        if (loan.getDataFinal().isBefore(loan.getDataInicial())) {
            throw new IllegalArgumentException("Data final deve ser maior que a data inicial");
        }
        
        if (loan.getPrimeiroPagamento().isBefore(loan.getDataInicial()) || 
            loan.getPrimeiroPagamento().isAfter(loan.getDataFinal())) {
            throw new IllegalArgumentException("Data do primeiro pagamento deve estar entre a data inicial e final");
        }
    }
} 