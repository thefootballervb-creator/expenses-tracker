package com.fullStack.expenseTracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.services.ReportService;

@RestController
@RequestMapping("/mypockit/report")
public class ReportController {

    @Autowired
    ReportService reportService;


    @GetMapping("/getTotalIncomeOrExpense")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalIncomeOrExpense(@Param("userId") Long userId,
                                                                     @Param("transactionTypeId") int transactionTypeId,
                                                                     @Param("month") int month,
                                                                     @Param("year") int year) {
        return reportService.getTotalByTransactionTypeAndUser(userId, transactionTypeId, month, year);
    }

    @GetMapping("/getTotalNoOfTransactions")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalNoOfTransactions(@Param("userId") Long userId,
                                                                      @Param("month") int month,
                                                                      @Param("year") int year) {
        return reportService.getTotalNoOfTransactionsByUser(userId, month, year);
    }

    @GetMapping("/getTotalByCategory")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getTotalByCategory(@Param("email") String email,
                                                                @Param("categoryId") int categoryId,
                                                                @Param("month") int month,
                                                                @Param("year") int year) {
        return reportService.getTotalExpenseByCategoryAndUser(email, categoryId, month, year);
    }

    @GetMapping("/getMonthlySummaryByUser")
    @PreAuthorize(("hasRole('ROLE_USER')"))
    public ResponseEntity<ApiResponseDto<?>> getMonthlySummaryByUser(@Param("email") String email) {
        return reportService.getMonthlySummaryByUser(email);
    }

    @GetMapping("/exportTransactions/pdf")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<byte[]> exportTransactionsPdf(@Param("email") String email) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportController.class);
        log.info("PDF export requested for email: {}", email);
        try {
            ResponseEntity<byte[]> response = reportService.exportUserTransactionsPdf(email);
            byte[] body = response.getBody();
            log.info("PDF export successful for email: {}, size: {} bytes", email, 
                    body != null ? body.length : 0);
            return response;
        } catch (Exception e) {
            log.error("PDF export failed for email: {}", email, e);
            throw e;
        }
    }

    @GetMapping("/exportTransactions/excel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<byte[]> exportTransactionsExcel(@Param("email") String email) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportController.class);
        log.info("Excel export requested for email: {}", email);
        try {
            ResponseEntity<byte[]> response = reportService.exportUserTransactionsExcel(email);
            byte[] body = response.getBody();
            log.info("Excel export successful for email: {}, size: {} bytes", email,
                    body != null ? body.length : 0);
            return response;
        } catch (Exception e) {
            log.error("Excel export failed for email: {}", email, e);
            throw e;
        }
    }

}
