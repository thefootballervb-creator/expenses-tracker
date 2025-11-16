package com.fullStack.expenseTracker.services.impls;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.reponses.TransactionsMonthlySummaryDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.models.Transaction;
import com.fullStack.expenseTracker.repository.TransactionRepository;
import com.fullStack.expenseTracker.services.ReportService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public ResponseEntity<ApiResponseDto<?>> getTotalByTransactionTypeAndUser(Long userId, int transactionTypeId, int month, int year) {
        Double total = transactionRepository.findTotalByUserAndTransactionType(userId, transactionTypeId, month, year);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                        HttpStatus.OK,
                        total != null ? total : 0.0
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getTotalNoOfTransactionsByUser(Long userId,  int month, int year) {
        Integer count = transactionRepository.findTotalNoOfTransactionsByUser(userId, month, year);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                        HttpStatus.OK,
                        count != null ? count : 0
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getTotalExpenseByCategoryAndUser(String email, int categoryId, int month, int year) {
        Double total = transactionRepository.findTotalByUserAndCategory(email, categoryId, month, year);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                        HttpStatus.OK,
                        total != null ? total : 0.0
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> getMonthlySummaryByUser(String email) {
        try {
            List<Object[]> result = transactionRepository.findMonthlySummaryByUser(email);

            if (result == null || result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                                HttpStatus.OK,
                                java.util.Collections.emptyList()
                        )
                );
            }

            List<TransactionsMonthlySummaryDto> transactionsMonthlySummary = result.stream()
                    .map(data -> new TransactionsMonthlySummaryDto(
                            (int) data[0],
                            data[1] != null ? (double) data[1] : 0.0,
                            data[2] != null ? (double) data[2] : 0.0
                    )).toList();

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            transactionsMonthlySummary
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching monthly summary for user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponseDto<>(ApiResponseStatus.SUCCESS,
                            HttpStatus.OK,
                            java.util.Collections.emptyList()
                    )
            );
        }
    }

    @Override
    public ResponseEntity<byte[]> exportUserTransactionsPdf(String email) {
        log.info("Exporting PDF for user: {}", email);
        if (email == null || email.trim().isEmpty()) {
            log.error("Email parameter is null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<Transaction> transactions = transactionRepository.findByUser_EmailOrderByDateDesc(email);
        if (transactions == null || transactions.isEmpty()) {
            log.warn("No transactions found for user: {}", email);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Transactions Report for " + email));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 5f, 2f});
            table.addCell(new PdfPCell(new Paragraph("Date")));
            table.addCell(new PdfPCell(new Paragraph("Category")));
            table.addCell(new PdfPCell(new Paragraph("Description")));
            table.addCell(new PdfPCell(new Paragraph("Amount")));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Transaction t : transactions) {
                table.addCell(new PdfPCell(new Paragraph(t.getDate() != null ? t.getDate().format(formatter) : "")));
                table.addCell(new PdfPCell(new Paragraph(
                        t.getCategory() != null ? t.getCategory().getCategoryName() : ""
                )));
                table.addCell(new PdfPCell(new Paragraph(t.getDescription() != null ? t.getDescription() : "")));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(t.getAmount()))));
            }

            document.add(table);
            document.close();

            byte[] pdfBytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "transactions-report.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export transactions PDF for user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> exportUserTransactionsExcel(String email) {
        log.info("Exporting Excel for user: {}", email);
        if (email == null || email.trim().isEmpty()) {
            log.error("Email parameter is null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<Transaction> transactions = transactionRepository.findByUser_EmailOrderByDateDesc(email);
        if (transactions == null || transactions.isEmpty()) {
            log.warn("No transactions found for user: {}", email);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Category");
            header.createCell(2).setCellValue("Description");
            header.createCell(3).setCellValue("Amount");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int rowIdx = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getDate() != null ? t.getDate().format(formatter) : "");
                row.createCell(1).setCellValue(
                        t.getCategory() != null ? t.getCategory().getCategoryName() : ""
                );
                row.createCell(2).setCellValue(t.getDescription() != null ? t.getDescription() : "");
                row.createCell(3).setCellValue(t.getAmount());
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            byte[] excelBytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            );
            headers.setContentDispositionFormData("attachment", "transactions-report.xlsx");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export transactions Excel for user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
