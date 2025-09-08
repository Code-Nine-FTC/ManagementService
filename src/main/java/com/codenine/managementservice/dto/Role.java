package com.codenine.managementservice.dto;

public enum Role {
    ADMIN(1),
    MANAGER(3),
    ASSISTANT(5);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
