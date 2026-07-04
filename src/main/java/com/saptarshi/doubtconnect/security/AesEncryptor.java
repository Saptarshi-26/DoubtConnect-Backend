package com.saptarshi.doubtconnect.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Converter
public class AesEncryptor implements AttributeConverter<String, String> {

    @Value("${aes.secret}")
    private String SECRET_KEY;

    private static final String ALGORITHM = "AES";

    private SecretKeySpec getKey() {

        if (SECRET_KEY == null || SECRET_KEY.length() != 16) {
            throw new IllegalStateException(
                    "aes.secret must be exactly 16 characters long.");
        }

        return new SecretKeySpec(
                SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                ALGORITHM
        );
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(
                            attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(cipher.doFinal(
                    Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}