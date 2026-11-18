package com.zamcan.codelab;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Pure Java mapping: a verified Appwrite user never shares the guest database. */
public final class LearnerDatabaseNames {
    private LearnerDatabaseNames() {}
    public static String forVerifiedUser(String userId) {
        if (userId == null || !userId.matches("[A-Za-z0-9._-]{1,128}"))
            throw new IllegalArgumentException("Invalid Appwrite user ID");
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(userId.getBytes(StandardCharsets.UTF_8));
            StringBuilder name = new StringBuilder("learning-user-");
            for (byte b : hash) {
                name.append(Character.forDigit((b >>> 4) & 15, 16));
                name.append(Character.forDigit(b & 15, 16));
            }
            return name.append(".db").toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
