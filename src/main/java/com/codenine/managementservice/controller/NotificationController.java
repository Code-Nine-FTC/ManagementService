package com.codenine.managementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.entity.Notification;
import com.codenine.managementservice.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  @Autowired private NotificationService notificationService;

  @Operation(description = "Lista todas as notificações não reconhecidas")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
  @GetMapping("/unacknowledged")
  public ResponseEntity<List<Notification>> getUnacknowledgedNotifications() {
    try {
      List<Notification> notifications = notificationService.getUnacknowledgedNotifications();
      return ResponseEntity.ok(notifications);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(description = "Marca uma notificação como reconhecida")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
  @PatchMapping("/{id}/acknowledge")
  public ResponseEntity<Void> acknowledgeNotification(
      @Parameter(description = "ID da notificação", example = "1") @PathVariable Long id) {
    try {
      notificationService.acknowledgeNotification(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(description = "Marca todas as notificações como reconhecidas")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
  @PatchMapping("/acknowledge-all")
  public ResponseEntity<Void> acknowledgeAllNotifications() {
    try {
      notificationService.acknowledgeAllNotifications();
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
