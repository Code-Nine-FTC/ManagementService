package com.codenine.managementservice.config;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.entity.*;
import com.codenine.managementservice.repository.*;
import com.codenine.managementservice.utils.CryptUtil;

@Component
public class ExcelItemImporter {

  @Autowired private ItemTypeRepository itemTypeRepository;
  @Autowired private SectionRepository sectionRepository;
  @Autowired private ItemRepository itemRepository;
  @Autowired private CryptUtil cryptUtil;
  @Autowired private LotRepository lotRepository;

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
  public void importItemsExcel(String excelPath, Long sectionId, User lastUser) throws Exception {

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

                  LocalDate expireDate = null;
                  if (validade != null && !validade.isEmpty()) {
                      String[] parts = validade.split("/");
                      if (parts.length == 2) {
                          try {
                              int month = Integer.parseInt(parts[0]);
                              int year = Integer.parseInt(parts[1]);
                              if (parts[1].trim().length() == 2) {
                                  year = 2000 + Integer.parseInt(parts[1].trim());
                              }
                              YearMonth ym = YearMonth.of(year, month);
                              expireDate = ym.atEndOfMonth();
                          } catch (NumberFormatException e) {
                              System.err.println("⚠ Validade inválida na linha " + (row.getRowNum() + 1));
                          }
                      }
                  }

                  Item existingItem = itemRepository.findByNameAndItemTypeSectionId(itemName, section.getId()).orElse(null);

                  if (existingItem != null) {
                      try {
                          Lot lot = new Lot();
                          lot.setItem(existingItem);
                          lot.setCode(lote != null ? lote : "");
                          lot.setExpireDate(expireDate);
                          lot.setQuantityOnHand(Math.max(0, qtd));
                          lotRepository.save(lot);
                          int total = lotRepository.findByItemOrderByExpireDateAscCreatedAtAsc(existingItem).stream()
                                  .mapToInt(Lot::getQuantityOnHand)
                                  .sum();
                          existingItem.setCurrentStock(total);
                          itemRepository.save(existingItem);
                      } catch (Exception e) {
                          System.err.println(
                                  "⚠ Erro ao salvar lote para item existente na linha "
                                          + (row.getRowNum() + 1)
                                          + ": "
                                          + e.getMessage());
                      }
                  } else {
                      try {
                          Item item = new Item();
                          item.setName(itemName);
                          item.setCurrentStock(0);
                          item.setLastUser(lastUser);
                          item.setMinimumStock(10);
                          item.setItemType(itemType);
                          item.setMeasure("unidade"); // valor padrão nunca nulo
//                          if (expireDate != null) item.setExpireDate(expireDate);
                          item.setIsActive(true);

                          Item savedId = itemRepository.save(item);
                          String qrCode = cryptUtil.encrypt(savedId.getId().toString());
                          item.setQrCode("/items/qr?code=" + qrCode);
                          itemRepository.save(savedId);

                          Lot lotObj = new Lot();
                          lotObj.setItem(savedId);
                          lotObj.setCode(lote != null ? lote : "");
                          lotObj.setExpireDate(expireDate);
                          lotObj.setQuantityOnHand(Math.max(0, qtd));
                          lotRepository.save(lotObj);

                          int total =
                                  lotRepository.findByItemOrderByExpireDateAscCreatedAtAsc(savedId).stream()
                                          .mapToInt(Lot::getQuantityOnHand)
                                          .sum();
                          savedId.setCurrentStock(total);
                          itemRepository.save(savedId);

                      } catch (Exception e) {
                          System.err.println(
                                  "⚠ Erro ao salvar item na linha " + (row.getRowNum() + 1) + ": " + e.getMessage());
                          continue;
                      }
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
