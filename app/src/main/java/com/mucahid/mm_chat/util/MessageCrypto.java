package com.mucahid.mm_chat.util;

import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.google.crypto.tink.TinkProtoKeysetFormat;
import com.google.crypto.tink.hybrid.HybridConfig;
import com.google.crypto.tink.subtle.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * End-to-end encryption of private messages (pure Java, no Android dependencies).
 *
 * <p>Each user has an HPKE key pair (X25519 + HKDF-SHA256 + AES-256-GCM). A message is encrypted
 * with the public key of whoever will read that copy; only the matching private key, which never
 * leaves the reader's device, can decrypt it. The ciphertext is bound to the message id and the
 * sender id (associated data), so it cannot be moved to another message or sender.
 */
public final class MessageCrypto {

    private static final String KEY_TEMPLATE = "DHKEM_X25519_HKDF_SHA256_HKDF_SHA256_AES_256_GCM";
    private static final String AAD_PREFIX = "me-chat/private-message/v1|";

    static {
        try {
            HybridConfig.register();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Tink hybrid encryption is unavailable", e);
        }
    }

    private MessageCrypto() {
    }

    /**
     * The key template for user key pairs. Always obtain it through this method (not a constant):
     * calling it guarantees the static initializer above has registered the HPKE templates.
     */
    public static KeyTemplate keyTemplate() throws GeneralSecurityException {
        return KeyTemplates.get(KEY_TEMPLATE);
    }

    /** Creates a new private keyset (used by tests; the app keeps its keyset in the Android Keystore). */
    public static KeysetHandle newPrivateKeyset() throws GeneralSecurityException {
        return KeysetHandle.generateNew(keyTemplate());
    }

    /** The public half of a private keyset, Base64 encoded, safe to publish. */
    public static String exportPublicKey(KeysetHandle privateKeyset) throws GeneralSecurityException {
        byte[] publicKeyset = TinkProtoKeysetFormat.serializeKeysetWithoutSecret(
                privateKeyset.getPublicKeysetHandle());
        return Base64.encode(publicKeyset);
    }

    /** Encrypts {@code plaintext} so that only the owner of {@code publicKey} can read it. */
    public static String encrypt(String publicKey, String plaintext, String messageId, String senderId)
            throws GeneralSecurityException {
        KeysetHandle publicKeyset = TinkProtoKeysetFormat.parseKeysetWithoutSecret(Base64.decode(publicKey));
        HybridEncrypt hybridEncrypt = publicKeyset.getPrimitive(RegistryConfiguration.get(), HybridEncrypt.class);
        byte[] ciphertext = hybridEncrypt.encrypt(
                plaintext.getBytes(StandardCharsets.UTF_8), associatedData(messageId, senderId));
        return Base64.encode(ciphertext);
    }

    /** Decrypts a message; throws if it was not encrypted for this keyset or was tampered with. */
    public static String decrypt(KeysetHandle privateKeyset, String ciphertext, String messageId, String senderId)
            throws GeneralSecurityException {
        HybridDecrypt hybridDecrypt = privateKeyset.getPrimitive(RegistryConfiguration.get(), HybridDecrypt.class);
        byte[] plaintext;
        try {
            plaintext = hybridDecrypt.decrypt(Base64.decode(ciphertext), associatedData(messageId, senderId));
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Ciphertext is not valid Base64", e);
        }
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private static byte[] associatedData(String messageId, String senderId) {
        return (AAD_PREFIX + messageId + "|" + senderId).getBytes(StandardCharsets.UTF_8);
    }
}
