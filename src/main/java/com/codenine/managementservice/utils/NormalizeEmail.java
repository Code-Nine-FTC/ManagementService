package com.codenine.managementservice.utils;

import java.text.Normalizer;

public class NormalizeEmail {
  public static String normalize(String email) {
    if (email == null)
      return null;
    email = email.trim().toLowerCase();
    int atIndex = email.indexOf("@");
    if (atIndex == -1)
      return email;
    String local = email.substring(0, atIndex);
    // Remove acentos
    local = Normalizer.normalize(local, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    String domain = email.substring(atIndex);
    return local + domain;
  }
}
