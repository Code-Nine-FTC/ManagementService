package com.codenine.managementservice.config;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.entity.*;
import com.codenine.managementservice.repository.*;

@Component
public class ExcelItemImporter {

  @Autowired private ItemTypeRepository itemTypeRepository;
  @Autowired private SectionRepository sectionRepository;
  @Autowired private ItemRepository itemRepository;

  @Transactional
  public void importItemTypesFromExcel(String excelPath, Long sectionId, User lastUser)
      throws Exception {
    try (FileInputStream fis = new FileInputStream(excelPath);
        Workbook workbook = new XSSFWorkbook(fis)) {

      Sheet sheet = workbook.getSheetAt(0);
      Section section = sectionRepository.findById(sectionId).orElseThrow();

      Set<String> tipos = new HashSet<>();
      for (Row row : sheet) {
        if (row.getRowNum() <= 1) continue; // pula linha 0 (título) e linha 1 (cabeçalho)
        String apres = getStringCellValue(row.getCell(2));
        if (apres == null || apres.isEmpty()) continue;
        if (apres.equals("*")) apres = "Outros";
        tipos.add(apres);
      }

      for (String tipo : tipos) {
        if (itemTypeRepository.findByNameAndSectionId(tipo, section.getId()) == null) {
          ItemType itemType = new ItemType();
          itemType.setName(tipo);
          itemType.setSection(section);
          itemType.setLastUser(lastUser);
          itemType.setLastUpdate(LocalDateTime.now());
          itemTypeRepository.save(itemType);
        }
      }
    }
  }

  @Transactional
  public void importItemsExcel(
      String excelPath, Long sectionId, User lastUser, SupplierCompany supplier) throws Exception {

    try (FileInputStream fis = new FileInputStream(excelPath);
        Workbook workbook = new XSSFWorkbook(fis)) {

      Sheet sheet = workbook.getSheetAt(0);
      Section section = sectionRepository.findById(sectionId).orElseThrow();

      for (Row row : sheet) {
        if (row.getRowNum() <= 1) continue; // pula título e cabeçalho

        try {
          String itemName = getStringCellValue(row.getCell(0));
          Integer qtd = getIntCellValue(row.getCell(1));
          String apres = getStringCellValue(row.getCell(2));
          String lote = getStringCellValue(row.getCell(3));
          String validade = getStringCellValue(row.getCell(4));

          if (itemName == null
              || itemName.isEmpty()
              || qtd == null
              || apres == null
              || apres.isEmpty()) {
            System.err.println(
                "⚠ Linha " + (row.getRowNum() + 1) + " ignorada (dados obrigatórios ausentes)");
            continue;
          }

          if ("*".equals(apres)) apres = "Outros";

          ItemType itemType = itemTypeRepository.findByNameAndSectionId(apres, section.getId());
          if (itemType == null) {
            itemType = new ItemType();
            itemType.setName(apres);
            itemType.setSection(section);
            itemType.setLastUser(lastUser);
            itemType.setLastUpdate(LocalDateTime.now());
            itemTypeRepository.save(itemType);
          }

          Item item = new Item();
          item.setName(itemName);
          item.setCurrentStock(qtd);
          item.setLastUser(lastUser);
          item.setMinimumStock(10);
          item.setItemType(itemType);
          item.setMeasure("unidade"); // valor padrão nunca nulo
          item.setSupplier(supplier);
          item.setQrCode(lote);

          if (validade != null && !validade.isEmpty()) {
            String[] parts = validade.split("/");
            if (parts.length == 2) {
              try {
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                LocalDateTime expireDate =
                    LocalDateTime.of(year, month, 1, 0, 0)
                        .withDayOfMonth(java.time.YearMonth.of(year, month).lengthOfMonth());
                item.setExpireDate(expireDate);
              } catch (NumberFormatException e) {
                System.err.println("⚠ Validade inválida na linha " + (row.getRowNum() + 1));
              }
            }
          }
          item.setIsActive(true);
          try {
            itemRepository.save(item);
          } catch (Exception e) {
            System.err.println(
                "⚠ Erro ao salvar item na linha " + (row.getRowNum() + 1) + ": " + e.getMessage());
            continue;
          }

        } catch (Exception e) {
          System.err.println(
              "⚠ Erro ao processar linha " + (row.getRowNum() + 1) + ": " + e.getMessage());
          // continua para próxima linha em vez de derrubar toda a transação
        }
      }
    }
  }

  private String getStringCellValue(Cell cell) {
    if (cell == null) return null;
    if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
    if (cell.getCellType() == CellType.NUMERIC)
      return String.valueOf((int) cell.getNumericCellValue());
    if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
    if (cell.getCellType() == CellType.BLANK) return null;
    return cell.toString();
  }

  private Integer getIntCellValue(Cell cell) {
    if (cell == null) return null;
    try {
      if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
      if (cell.getCellType() == CellType.STRING) {
        String value = cell.getStringCellValue().trim();
        if (value.isEmpty()) return null;
        return Integer.parseInt(value);
      }
      return null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
