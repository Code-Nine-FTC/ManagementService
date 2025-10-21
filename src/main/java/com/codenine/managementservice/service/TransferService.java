package com.codenine.managementservice.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Transfer;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.TransferRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransferService {

  @Autowired private TransferRepository transferRepository;

  @Autowired private NotificationService notificationService;

  public void createTransfer(Transfer transfer) {
    transferRepository.save(transfer);
    
    notificationService.createNotificationForTransfer(
        NotificationType.TRANSFER_CREATED,
        "Nova transferência #" + transfer.getId() + " criada",
        NotificationSeverity.INFO,
        transfer,
        7776000L); // 90 dias
  }

  public void cancelTransfer(Long transferId, User lastUser) {
    Transfer transfer = getTransferById(transferId);
    transfer.setStatus("CANCELLED");
    transfer.setLastUser(lastUser);
    transfer.setLastUpdate(LocalDateTime.now());
    transferRepository.save(transfer);
    
    notificationService.createNotificationForTransfer(
        NotificationType.TRANSFER_STATUS_CHANGED,
        "Transferência #" + transferId + " foi cancelada",
        NotificationSeverity.CRITICAL,
        transfer,
        7776000L); // 90 dias
  }

  public void completeTransfer(Long transferId, User lastUser) {
    Transfer transfer = getTransferById(transferId);
    transfer.setStatus("COMPLETED");
    transfer.setLastUser(lastUser);
    transfer.setLastUpdate(LocalDateTime.now());
    transferRepository.save(transfer);
    
    notificationService.createNotificationForTransfer(
        NotificationType.TRANSFER_STATUS_CHANGED,
        "Transferência #" + transferId + " foi concluída",
        NotificationSeverity.SUCCESS,
        transfer,
        7776000L); // 90 dias
  }

  private Transfer getTransferById(Long id) {
    return transferRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Transferência com ID " + id + " não encontrada."));
  }
}
