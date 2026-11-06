package com.mucahid.mm_chat.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Safety numbers for verifying end-to-end encryption, modelled on Signal's.
 *
 * <p>Each user's fingerprint is 30 digits derived from their user id and public key with 5200
 * rounds of SHA-512 (making it expensive to brute-force a different key with the same digits).
 * The safety number of a conversation is both fingerprints concatenated in user-id order, so both
 * people see the same 60 digits. If the digits match on both phones, nobody has swapped a key.
 */
public final class SafetyNumber {

    private static final String VERSION = "me-chat/safety-number/v1";
    private static final int ITERATIONS = 5200;
    private static final int CHUNKS = 6;
    private static final int CHUNK_BYTES = 5;

    private SafetyNumber() {
    }

    /** The 60-digit safety number for two users; identical no matter which side computes it. */
    public static String compute(String userIdA, String publicKeyA, String userIdB, String publicKeyB) {
        String fingerprintA = fingerprint(userIdA, publicKeyA);
        String fingerprintB = fingerprint(userIdB, publicKeyB);
        return userIdA.compareTo(userIdB) <= 0 ? fingerprintA + fingerprintB : fingerprintB + fingerprintA;
    }

    /** "12345 67890 ..." in 12 groups of five, three groups per line. */
    public static String format(String safetyNumber) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < safetyNumber.length(); i += 5) {
            if (i > 0) {
                formatted.append(i % 15 == 0 ? '\n' : ' ');
            }
            formatted.append(safetyNumber, i, Math.min(i + 5, safetyNumber.length()));
        }
        return formatted.toString();
    }

    static String fingerprint(String userId, String publicKey) {
        byte[] key = publicKey.getBytes(StandardCharsets.UTF_8);
        byte[] hash = concat((VERSION + "|" + userId + "|").getBytes(StandardCharsets.UTF_8), key);
        MessageDigest sha512 = sha512();
        for (int i = 0; i < ITERATIONS; i++) {
            sha512.update(hash);
            hash = sha512.digest(key);
        }

        StringBuilder digits = new StringBuilder();
        for (int chunk = 0; chunk < CHUNKS; chunk++) {
            long value = 0;
            for (int b = 0; b < CHUNK_BYTES; b++) {
                value = (value << 8) | (hash[chunk * CHUNK_BYTES + b] & 0xFF);
            }
            digits.append(String.format(java.util.Locale.ROOT, "%05d", value % 100000));
        }
        return digits.toString();
    }

    private static MessageDigest sha512() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 is required", e);
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
