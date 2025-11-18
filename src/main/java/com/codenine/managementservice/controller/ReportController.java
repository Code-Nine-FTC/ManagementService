package com.codenine.managementservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

  @Autowired private ReportService reportService;

  @GetMapping("/stock")
  public ResponseEntity<?> getStockReport(
      @RequestParam(required = false) Long sectionId, @RequestParam(defaultValue = "pdf") String format)
      throws Exception {
    String lower = format == null ? "pdf" : format.toLowerCase();
    if ("excel".equals(lower) || "xlsx".equals(lower)) {
      byte[] data = reportService.generateStockReportExcel(sectionId);
      ByteArrayResource resource = new ByteArrayResource(data);
      String filename = "stock-report" + (sectionId == null ? "-all" : "-" + sectionId) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .contentLength(data.length)
          .body(resource);
    }

    // default pdf
    byte[] data = reportService.generateStockReportPdf(sectionId);
    ByteArrayResource resource = new ByteArrayResource(data);
    String filename = "stock-report" + (sectionId == null ? "-all" : "-" + sectionId) + ".pdf";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(data.length)
        .body(resource);
  }

  @GetMapping("/orders")
  public ResponseEntity<?> getOrdersReport(@RequestParam(defaultValue = "pdf") String format)
      throws Exception {
    String lower = format == null ? "pdf" : format.toLowerCase();
    if ("excel".equals(lower) || "xlsx".equals(lower)) {
      byte[] data = reportService.generateOrdersReportExcel();
      ByteArrayResource resource = new ByteArrayResource(data);
      String filename = "orders-report.xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .contentLength(data.length)
          .body(resource);
    }

    byte[] data = reportService.generateOrdersReportPdf();
    ByteArrayResource resource = new ByteArrayResource(data);
    String filename = "orders-report.pdf";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(data.length)
        .body(resource);
  }
}
