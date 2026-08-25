package com.financetracker.controller;

import com.financetracker.domain.entity.User;
import com.financetracker.dto.response.BalanceHistoryResponse;
import com.financetracker.dto.response.CategoryBreakdownResponse;
import com.financetracker.dto.response.MonthlySummaryResponse;
import com.financetracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Spending summaries and balance analytics")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Monthly income vs expense summary (defaults to current month)")
    public MonthlySummaryResponse getMonthlySummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        return reportService.getMonthlySummary(user, ym.getYear(), ym.getMonthValue());
    }

    @GetMapping("/by-category")
    @Operation(summary = "Expense breakdown by category for a date range")
    public List<CategoryBreakdownResponse> getCategoryBreakdown(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return reportService.getCategoryBreakdown(user, startDate, endDate);
    }

    @GetMapping("/balance-history")
    @Operation(summary = "Running balance history for a specific account over a date range")
    public List<BalanceHistoryResponse> getBalanceHistory(
            @AuthenticationPrincipal User user,
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return reportService.getBalanceHistory(user, accountId, startDate, endDate);
    }
}
