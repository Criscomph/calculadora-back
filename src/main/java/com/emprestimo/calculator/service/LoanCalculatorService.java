package com.emprestimo.calculator.service;

import com.emprestimo.calculator.model.LoanCalculation;
import com.emprestimo.calculator.dto.InstallmentDTO;
import com.emprestimo.calculator.util.LoanConstants;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LoanCalculatorService {

    public List<InstallmentDTO> calculateLoan(LoanCalculation loan) {
        validateDates(loan);
        
        List<InstallmentDTO> installments = new ArrayList<>();
        
        // Adiciona a data inicial
        addInstallment(installments, loan.getDataInicial(), calculateInitialAmount(loan), 0);
        
        // Adiciona as parcelas mensais
        LocalDate currentDate = loan.getPrimeiroPagamento();
        int installmentNumber = 1;
        
        while (!currentDate.isAfter(loan.getDataFinal())) {
            // Ajusta para o último dia do mês se necessário
            LocalDate adjustedDate = adjustToMonthEnd(currentDate);
            
            // Calcula o valor da parcela
            double installmentAmount = calculateInstallmentAmount(loan, installmentNumber);
            
            // Adiciona a parcela
            addInstallment(installments, adjustedDate, installmentAmount, installmentNumber);
            
            // Move para o próximo mês mantendo o mesmo dia
            currentDate = currentDate.plusMonths(1);
            installmentNumber++;
        }
        
        // Adiciona datas de fim de mês entre a data inicial e o primeiro pagamento
        addEndOfMonthDates(installments, loan.getDataInicial(), loan.getPrimeiroPagamento(), loan);
        
        return installments;
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
    
    private double calculateInitialAmount(LoanCalculation loan) {
        return -loan.getValorEmprestimo();
    }
    
    private double calculateInstallmentAmount(LoanCalculation loan, int installmentNumber) {
        int totalInstallments = calculateTotalInstallments(loan);
        double monthlyRate = loan.getTaxaJuros() / LoanConstants.PERCENTAGE_DIVISOR / LoanConstants.MONTHS_IN_YEAR;
        
        // Fórmula PMT para cálculo das parcelas
        double pmt = loan.getValorEmprestimo() * 
                    (monthlyRate * Math.pow(1 + monthlyRate, totalInstallments)) /
                    (Math.pow(1 + monthlyRate, totalInstallments) - 1);
                    
        return pmt;
    }
    
    private int calculateTotalInstallments(LoanCalculation loan) {
        return (int) ChronoUnit.MONTHS.between(
            loan.getPrimeiroPagamento().withDayOfMonth(1),
            loan.getDataFinal().withDayOfMonth(1)
        ) + 1;
    }
    
    private LocalDate adjustToMonthEnd(LocalDate date) {
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        return date.getDayOfMonth() > lastDayOfMonth.getDayOfMonth() ? lastDayOfMonth : date;
    }
    
    private void addEndOfMonthDates(List<InstallmentDTO> installments, LocalDate startDate, LocalDate endDate, LoanCalculation loan) {
        LocalDate currentDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        while (currentDate.isBefore(endDate)) {
            if (!currentDate.equals(startDate)) {
                addInstallment(installments, currentDate, 0.0, 0);
            }
            currentDate = currentDate.plusMonths(1).withDayOfMonth(currentDate.plusMonths(1).lengthOfMonth());
        }
    }
    
    private void addInstallment(List<InstallmentDTO> installments, LocalDate date, Double amount, Integer installmentNumber) {
        InstallmentDTO installment = new InstallmentDTO();
        installment.setDataCompetencia(date);
        installment.setValorEmprestimo(amount);
        installment.setParcela(amount);
        installment.setSaldoDevedor(0.0);
        installment.setTotal(0.0);
        installment.setAmortizacao(0.0);
        installment.setSaldo(0.0);
        installment.setProvisao(0.0);
        installment.setAcumulado(0.0);
        installment.setPago(false);
        installments.add(installment);
    }
} 