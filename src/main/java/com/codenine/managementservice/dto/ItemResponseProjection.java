package com.codenine.managementservice.dto;

import java.sql.Timestamp;

public interface ItemResponseProjection {
    Long getItemId();
    String getName();
    Integer getCurrentStock();
    String getMeasure();
    Timestamp getExpireDate();
    Long getSupplierId();
    String getSupplierName();
    Long getSectionId();
    String getSectionName();
    Long getItemTypeId();
    String getItemTypeName();
    Integer getMinimumStock();
    String getQrCode();
    String getLastUserName();
    Timestamp getLastUpdate();
}
