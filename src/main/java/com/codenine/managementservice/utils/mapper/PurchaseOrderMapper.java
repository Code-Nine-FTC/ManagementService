package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;
import java.util.Random;

import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderRequest;
import com.codenine.managementservice.entity.PurchaseOrder;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;

public class PurchaseOrderMapper {

  private static final Random random = new Random();

  private static String generatePurchaseOrderNumber() {
    LocalDateTime now = LocalDateTime.now();
    int year = now.getYear();
    int randomNumber = 1000 + random.nextInt(9000);
    
    return "OC-" + year + "-" + randomNumber;
  }

  public static PurchaseOrder toEntity(
      PurchaseOrderRequest request, User lastUser, SupplierCompany supplierCompany) {
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setPurchaseOrderNumber(generatePurchaseOrderNumber());
    purchaseOrder.setIssuingBody(request.issuingBody());
    purchaseOrder.setCommitmentNoteNumber(request.commitmentNoteNumber());
    purchaseOrder.setYear(request.year());
    purchaseOrder.setProcessNumber(request.processNumber());
    purchaseOrder.setTotalValue(request.totalValue());
    purchaseOrder.setIssueDate(request.issueDate());
    purchaseOrder.setSupplierCompany(supplierCompany);
    purchaseOrder.setLastUser(lastUser);
    purchaseOrder.setCreatedBy(lastUser);
    return purchaseOrder;
  }

  public static void updateEntity(
      PurchaseOrder purchaseOrder,
      PurchaseOrderRequest request,
      User lastUser,
      SupplierCompany supplierCompany) {
    boolean isUpdated = false;

    if (request.issuingBody() != null) {
      purchaseOrder.setIssuingBody(request.issuingBody());
      isUpdated = true;
    }
    if (request.commitmentNoteNumber() != null) {
      purchaseOrder.setCommitmentNoteNumber(request.commitmentNoteNumber());
      isUpdated = true;
    }
    if (request.year() != null) {
      purchaseOrder.setYear(request.year());
      isUpdated = true;
    }
    if (request.processNumber() != null) {
      purchaseOrder.setProcessNumber(request.processNumber());
      isUpdated = true;
    }
    if (request.totalValue() != null) {
      purchaseOrder.setTotalValue(request.totalValue());
      isUpdated = true;
    }
    if (request.issueDate() != null) {
      purchaseOrder.setIssueDate(request.issueDate());
      isUpdated = true;
    }
    if (request.supplierCompanyId() != null) {
      purchaseOrder.setSupplierCompany(supplierCompany);
      isUpdated = true;
    }

    if (isUpdated) {
      purchaseOrder.setLastUpdate(LocalDateTime.now());
      purchaseOrder.setLastUser(lastUser);
    }
  }
}
