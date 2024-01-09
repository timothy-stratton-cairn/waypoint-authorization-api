package com.cairnfg.waypoint.authorization.entity.enumeration;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum AccountType {
    CLIENT(1L, "Client", "Clients are users who utilize a financial service"),
    USER(2L, "User", "Users are employees of Financial Group with base level permissions to enter and edit data"),
    ADMIN(3L, "Admin", "Admin users are employees of the Financial Group with all permissions available.");

    private final Long id;
    private final String name;
    private final String description;

    AccountType(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static AccountType fromId(Long id) {
        return Stream.of(AccountType.values())
                .filter(accountType -> Objects.equals(accountType.id, id))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
