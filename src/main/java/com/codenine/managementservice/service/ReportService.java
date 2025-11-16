package com.codenine.managementservice.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.repository.ItemRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ReportService {

  @Autowired private ItemRepository itemRepository;

  public byte[] generateStockReportPdf(Long sectionId) throws Exception {
    List<ItemResponse> allItems =
        itemRepository.findAllItemResponses(null, sectionId, null, null, null);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime future = now.plusDays(30);

    List<ItemResponse> itemsInLack =
        allItems.stream().filter(i -> i.currentStock() == null || i.currentStock() <= 0).collect(Collectors.toList());

    List<ItemResponse> itemsMinStock =
        allItems.stream()
            .filter(i -> i.minimumStock() != null && i.currentStock() != null && i.currentStock() <= i.minimumStock())
            .collect(Collectors.toList());

    List<ItemResponse> expired =
        allItems.stream()
            .filter(i -> i.expireDate() != null && i.expireDate().isBefore(now))
            .sorted((a, b) -> a.expireDate().compareTo(b.expireDate()))
            .collect(Collectors.toList());

    List<ItemResponse> expiringSoon =
        allItems.stream()
            .filter(i -> i.expireDate() != null && (i.expireDate().isAfter(now) || i.expireDate().isEqual(now)) && i.expireDate().isBefore(future))
            .sorted((a, b) -> a.expireDate().compareTo(b.expireDate()))
            .collect(Collectors.toList());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter.getInstance(doc, baos);
    doc.open();

    Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

    doc.add(new Paragraph("Relatório de Estoque", h1));
    doc.add(new Paragraph("Seção: " + (sectionId == null ? "Todas" : sectionId.toString()), normal));
    doc.add(new Paragraph("Gerado em: " + now.toString(), normal));
    doc.add(Paragraph.getInstance(""));

    // Itens em falta
    doc.add(new Paragraph("Itens em falta", h2));
    addItemsTableToPdf(doc, itemsInLack);

    // Estoque mínimo
    doc.add(new Paragraph("Itens no estoque mínimo", h2));
    addItemsTableToPdf(doc, itemsMinStock);

    // Vencidos
    doc.add(new Paragraph("Vencidos", h2));
    addItemsTableToPdf(doc, expired);

    // Próximos do vencimento
    doc.add(new Paragraph("Próximos do vencimento (30 dias)", h2));
    addItemsTableToPdf(doc, expiringSoon);

    // Lista completa
    doc.add(new Paragraph("Lista completa de itens", h2));
    addItemsTableToPdf(doc, allItems);

    doc.close();
    return baos.toByteArray();
  }

  private void addItemsTableToPdf(Document doc, List<ItemResponse> items) throws Exception {
    if (items == null || items.isEmpty()) {
      doc.add(new Paragraph("Nenhum registro.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
      return;
    }

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    table.setWidths(new int[] {3, 2, 1, 2, 2});
    table.addCell(createCellHeader("Nome"));
    table.addCell(createCellHeader("Tipo"));
    table.addCell(createCellHeader("Qtd"));
    table.addCell(createCellHeader("Validade"));
    table.addCell(createCellHeader("Seção"));

    for (ItemResponse it : items) {
      table.addCell(createCell(it.name()));
      table.addCell(createCell(it.itemTypeName()));
      table.addCell(createCell(it.currentStock() == null ? "0" : it.currentStock().toString()));
      table.addCell(createCell(it.expireDate() == null ? "—" : it.expireDate().toLocalDate().toString()));
      table.addCell(createCell(it.sectionName()));
    }

    doc.add(table);
  }

  private PdfPCell createCellHeader(String text) {
    PdfPCell c = new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
    c.setHorizontalAlignment(Element.ALIGN_LEFT);
    return c;
  }

  private PdfPCell createCell(String text) {
    PdfPCell c = new PdfPCell(new Paragraph(text == null ? "" : text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
    c.setHorizontalAlignment(Element.ALIGN_LEFT);
    return c;
  }

  public byte[] generateStockReportExcel(Long sectionId) throws Exception {
    List<ItemResponse> allItems =
        itemRepository.findAllItemResponses(null, sectionId, null, null, null);

    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Sheet s = wb.createSheet("Relatório Estoque");
      int rownum = 0;

      Row h = s.createRow(rownum++);
      Cell ch = h.createCell(0);
      ch.setCellValue("Relatório de Estoque");

      Row info = s.createRow(rownum++);
      info.createCell(0).setCellValue("Seção:");
      info.createCell(1).setCellValue(sectionId == null ? "Todas" : sectionId.toString());
      info.createCell(2).setCellValue("Gerado em:");
      info.createCell(3).setCellValue(LocalDateTime.now().toString());

      rownum++;

      // headers
      Row header = s.createRow(rownum++);
      header.createCell(0).setCellValue("Nome");
      header.createCell(1).setCellValue("Tipo");
      header.createCell(2).setCellValue("Qtd");
      header.createCell(3).setCellValue("Validade");
      header.createCell(4).setCellValue("Seção");

      for (ItemResponse it : allItems) {
        Row r = s.createRow(rownum++);
        r.createCell(0).setCellValue(it.name());
        r.createCell(1).setCellValue(it.itemTypeName() == null ? "" : it.itemTypeName());
        r.createCell(2).setCellValue(it.currentStock() == null ? 0 : it.currentStock());
        r.createCell(3).setCellValue(it.expireDate() == null ? "" : it.expireDate().toLocalDate().toString());
        r.createCell(4).setCellValue(it.sectionName() == null ? "" : it.sectionName());
      }

      for (int i = 0; i < 6; i++) s.autoSizeColumn(i);

      wb.write(baos);
      return baos.toByteArray();
    }
  }
}
