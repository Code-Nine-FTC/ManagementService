package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name =  "transfers")
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    
    private long deliveryDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();

    @ManyToOne
    private User lastUser;

    @ManyToOne
    private Item item;

    @ManyToOne
    private UnitDestination unitDestination;
}
