package com.codenine.managementservice.inject;

import org.springframework.stereotype.Component;

import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.config.ExcelItemImporter;

import java.util.List;

@Component
public class ExcelItemBootstrap {

  private final ExcelItemImporter excelItemImporter;
  public ExcelItemBootstrap(
      ExcelItemImporter excelItemImporter) {
    this.excelItemImporter = excelItemImporter;
  }

  public void importTypesAndItems(List<Section> sections, User adminUser) {
    for (Section section : sections) {
      String filePath =
          section.getTitle().equalsIgnoreCase("Almoxarifado")
              ? "src/main/resources/almoxarifado.xlsx"
              : "src/main/resources/farmacia.xlsx";
      try {
        excelItemImporter.importItemTypesFromExcel(filePath, section.getId(), adminUser);
        excelItemImporter.importItemsExcel(filePath, section.getId(), adminUser);
      } catch (Exception e) {
        System.err.println("Erro ao importar itens/ tipos para seção " + section.getTitle() + ": " + e.getMessage());
      }
    }
  }
}
