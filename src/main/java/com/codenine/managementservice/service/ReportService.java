package com.codenine.managementservice.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.OrderItem;

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
  @Autowired private OrderRepository orderRepository;
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    doc.add(new Paragraph("Gerado em: " + now.format(DATE_TIME_FMT), normal));
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
      table.addCell(createCell(it.expireDate() == null ? "—" : formatDateLike(it.expireDate(), DATE_FMT)));
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
    info.createCell(3).setCellValue(LocalDateTime.now().format(DATE_TIME_FMT));

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
  r.createCell(3).setCellValue(it.expireDate() == null ? "" : formatDateLike(it.expireDate(), DATE_FMT));
        r.createCell(4).setCellValue(it.sectionName() == null ? "" : it.sectionName());
      }

      for (int i = 0; i < 6; i++) s.autoSizeColumn(i);

      wb.write(baos);
      return baos.toByteArray();
    }
  }

  public byte[] generateOrdersReportPdf() throws Exception {
    List<Order> orders = orderRepository.findAll();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter.getInstance(doc, baos);
    doc.open();

    Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

  doc.add(new Paragraph("Relatório de Pedidos", h1));
  doc.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DATE_TIME_FMT), normal));
    doc.add(Paragraph.getInstance(""));

    if (orders.isEmpty()) {
      doc.add(new Paragraph("Nenhum pedido encontrado.", normal));
      doc.close();
      return baos.toByteArray();
    }

    for (Order o : orders) {
      doc.add(new Paragraph("Pedido #" + o.getId() + " - " + (o.getOrderNumber() == null ? "" : o.getOrderNumber()), h2));
  PdfPTable meta = new PdfPTable(4);
      meta.setWidthPercentage(100);
      meta.addCell(createCell("Seção: " + (o.getSection() != null ? o.getSection().getTitle() : "—")));
      meta.addCell(createCell("Status: " + (o.getStatus() == null ? "—" : o.getStatus())));
      meta.addCell(createCell("Responsável: " + (o.getCreatedBy() != null ? o.getCreatedBy().getName() : "—")));
  meta.addCell(createCell("Última atualização: " + (o.getLastUpdate() != null ? formatDateLike(o.getLastUpdate(), DATE_TIME_FMT) : "—")));
      doc.add(meta);

      // items table
      List<OrderItem> items = o.getOrderItems();
      if (items == null || items.isEmpty()) {
        doc.add(new Paragraph("  Sem itens.", normal));
      } else {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[] {4, 1, 1, 2});
        table.addCell(createCellHeader("Item"));
        table.addCell(createCellHeader("Qtd"));
        table.addCell(createCellHeader("Un."));
        table.addCell(createCellHeader("Observações"));
        for (OrderItem oi : items) {
          table.addCell(createCell(oi.getItem() != null ? oi.getItem().getName() : "—"));
          table.addCell(createCell(String.valueOf(oi.getQuantity())));
          table.addCell(createCell(oi.getItem() != null ? oi.getItem().getMeasure() : "—"));
          table.addCell(createCell(""));
        }
        doc.add(table);
      }

      doc.add(Paragraph.getInstance(""));
    }

    doc.close();
    return baos.toByteArray();
  }

  public byte[] generateOrdersReportExcel() throws Exception {
    List<Order> orders = orderRepository.findAll();
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Sheet s = wb.createSheet("Pedidos");
      int rownum = 0;

      Row h = s.createRow(rownum++);
      h.createCell(0).setCellValue("Relatório de Pedidos");
      Row info = s.createRow(rownum++);
      info.createCell(0).setCellValue("Gerado em:");
  info.createCell(1).setCellValue(LocalDateTime.now().format(DATE_TIME_FMT));
      rownum++;

      // header for orders
      Row header = s.createRow(rownum++);
      header.createCell(0).setCellValue("PedidoId");
      header.createCell(1).setCellValue("OrderNumber");
      header.createCell(2).setCellValue("Seção");
      header.createCell(3).setCellValue("Status");
      header.createCell(4).setCellValue("Responsável");
      header.createCell(5).setCellValue("Última atualização");

      for (Order o : orders) {
        Row r = s.createRow(rownum++);
        r.createCell(0).setCellValue(o.getId());
        r.createCell(1).setCellValue(o.getOrderNumber() == null ? "" : o.getOrderNumber());
        r.createCell(2).setCellValue(o.getSection() != null ? o.getSection().getTitle() : "");
        r.createCell(3).setCellValue(o.getStatus() == null ? "" : o.getStatus());
        r.createCell(4).setCellValue(o.getCreatedBy() != null ? o.getCreatedBy().getName() : "");
  r.createCell(5).setCellValue(o.getLastUpdate() != null ? formatDateLike(o.getLastUpdate(), DATE_TIME_FMT) : "");

        // items as subsequent rows
        List<OrderItem> items = o.getOrderItems();
        if (items != null && !items.isEmpty()) {
          Row subHeader = s.createRow(rownum++);
          subHeader.createCell(1).setCellValue("Item");
          subHeader.createCell(2).setCellValue("Qtd");
          subHeader.createCell(3).setCellValue("Un.");
          for (OrderItem oi : items) {
            Row ir = s.createRow(rownum++);
            ir.createCell(1).setCellValue(oi.getItem() != null ? oi.getItem().getName() : "");
            ir.createCell(2).setCellValue(oi.getQuantity());
            ir.createCell(3).setCellValue(oi.getItem() != null ? oi.getItem().getMeasure() : "");
          }
        }
      }

      for (int i = 0; i < 8; i++) s.autoSizeColumn(i);
      wb.write(baos);
      return baos.toByteArray();
    }
  }

  private String formatDateLike(LocalDateTime dt, DateTimeFormatter fmt) {
    if (dt == null) return "—";
    return dt.format(fmt);
  }

  private String formatDateLike(LocalDate d, DateTimeFormatter fmt) {
    if (d == null) return "—";
    return d.format(fmt);
  }
}
