package com.codenine.managementservice.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptUtil {
  public final String key;

  public CryptUtil(@Value("${crypto.secret}") String secret) {
    this.key = secret;
  }

  public String encrypt(String data) {
    try {
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encrypted = cipher.doFinal(data.getBytes());
      return Base64.getUrlEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      throw new RuntimeException("Erro ao criptografar", e);
    }
  }

  public String decrypt(String encrypted) {
    try {
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] decoded = Base64.getUrlDecoder().decode(encrypted);
      return new String(cipher.doFinal(decoded));
    } catch (Exception e) {
      throw new RuntimeException("Erro ao descriptografar", e);
    }
  }
}
