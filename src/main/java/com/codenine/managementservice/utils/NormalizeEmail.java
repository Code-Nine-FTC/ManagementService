package com.codenine.managementservice.utils;

public class NormalizeEmail {
    public static String normalize(String email) {
        if (email == null) return null;
        email = email.trim().toLowerCase();
        int atIndex = email.indexOf("@");
        if (atIndex == -1) return email;
        String local = email.substring(0, atIndex).replaceAll("[^a-z0-9._]", "");
        String domain = email.substring(atIndex);
        return local + domain;
    }
}
