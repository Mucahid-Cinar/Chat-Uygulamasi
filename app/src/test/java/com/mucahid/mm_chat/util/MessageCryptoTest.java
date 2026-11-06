package com.mucahid.mm_chat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.subtle.Base64;

import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;

public class MessageCryptoTest {

    private static final String MESSAGE_ID = "-OAbCdEfGhIjKlMnOp";
    private static final String SENDER_ID = "aliceUid";

    private KeysetHandle aliceKeys;
    private KeysetHandle bobKeys;
    private String bobPublicKey;

    @Before
    public void setUp() throws GeneralSecurityException {
        aliceKeys = MessageCrypto.newPrivateKeyset();
        bobKeys = MessageCrypto.newPrivateKeyset();
        bobPublicKey = MessageCrypto.exportPublicKey(bobKeys);
    }

    @Test
    public void recipientCanDecrypt() throws GeneralSecurityException {
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "Merhaba bob", MESSAGE_ID, SENDER_ID);

        assertEquals("Merhaba bob", MessageCrypto.decrypt(bobKeys, ciphertext, MESSAGE_ID, SENDER_ID));
    }

    @Test
    public void ciphertextDoesNotContainPlaintext() throws GeneralSecurityException {
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "gizli mesaj", MESSAGE_ID, SENDER_ID);

        assertEquals(-1, new String(Base64.decode(ciphertext)).indexOf("gizli mesaj"));
    }

    @Test
    public void otherKeyCannotDecrypt() throws GeneralSecurityException {
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "sadece bob okur", MESSAGE_ID, SENDER_ID);

        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(aliceKeys, ciphertext, MESSAGE_ID, SENDER_ID));
    }

    @Test
    public void ciphertextIsBoundToMessageId() throws GeneralSecurityException {
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "mesaj", MESSAGE_ID, SENDER_ID);

        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(bobKeys, ciphertext, "-OtherMessageId", SENDER_ID));
    }

    @Test
    public void ciphertextIsBoundToSender() throws GeneralSecurityException {
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "mesaj", MESSAGE_ID, SENDER_ID);

        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(bobKeys, ciphertext, MESSAGE_ID, "eveUid"));
    }

    @Test
    public void tamperedCiphertextIsRejected() throws GeneralSecurityException {
        byte[] bytes = Base64.decode(MessageCrypto.encrypt(bobPublicKey, "mesaj", MESSAGE_ID, SENDER_ID));
        bytes[bytes.length - 1] ^= 1;
        String tampered = Base64.encode(bytes);

        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(bobKeys, tampered, MESSAGE_ID, SENDER_ID));
    }

    @Test
    public void invalidBase64IsRejected() {
        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(bobKeys, "!!! not base64 !!!", MESSAGE_ID, SENDER_ID));
    }

    @Test
    public void sameMessageEncryptsDifferentlyEachTime() throws GeneralSecurityException {
        String first = MessageCrypto.encrypt(bobPublicKey, "ayni", MESSAGE_ID, SENDER_ID);
        String second = MessageCrypto.encrypt(bobPublicKey, "ayni", MESSAGE_ID, SENDER_ID);

        assertNotEquals(first, second);
    }

    @Test
    public void unicodeRoundTrip() throws GeneralSecurityException {
        String text = "Çğıöşü ÇĞİÖŞÜ 😀 — ✓";
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, text, MESSAGE_ID, SENDER_ID);

        assertEquals(text, MessageCrypto.decrypt(bobKeys, ciphertext, MESSAGE_ID, SENDER_ID));
    }

    @Test
    public void publicKeyContainsNoSecret() throws GeneralSecurityException {
        // A public keyset can encrypt but must not be usable to decrypt.
        String ciphertext = MessageCrypto.encrypt(bobPublicKey, "mesaj", MESSAGE_ID, SENDER_ID);
        KeysetHandle publicOnly = bobKeys.getPublicKeysetHandle();

        assertThrows(GeneralSecurityException.class,
                () -> MessageCrypto.decrypt(publicOnly, ciphertext, MESSAGE_ID, SENDER_ID));
    }
}
