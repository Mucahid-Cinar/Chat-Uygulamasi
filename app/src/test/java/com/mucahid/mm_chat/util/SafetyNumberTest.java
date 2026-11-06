package com.mucahid.mm_chat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SafetyNumberTest {

    private static final String ALICE = "aliceUid";
    private static final String BOB = "bobUid";
    private static final String ALICE_KEY = "QUxJQ0UtUFVCTElDLUtFWQ==";
    private static final String BOB_KEY = "Qk9CLVBVQkxJQy1LRVk=";

    @Test
    public void bothSidesSeeTheSameNumber() {
        assertEquals(
                SafetyNumber.compute(ALICE, ALICE_KEY, BOB, BOB_KEY),
                SafetyNumber.compute(BOB, BOB_KEY, ALICE, ALICE_KEY));
    }

    @Test
    public void numberIsSixtyDigits() {
        String number = SafetyNumber.compute(ALICE, ALICE_KEY, BOB, BOB_KEY);

        assertEquals(60, number.length());
        assertTrue(number.matches("[0-9]{60}"));
    }

    @Test
    public void isDeterministic() {
        assertEquals(
                SafetyNumber.compute(ALICE, ALICE_KEY, BOB, BOB_KEY),
                SafetyNumber.compute(ALICE, ALICE_KEY, BOB, BOB_KEY));
    }

    @Test
    public void changesWhenEitherKeyChanges() {
        String original = SafetyNumber.compute(ALICE, ALICE_KEY, BOB, BOB_KEY);

        assertNotEquals(original, SafetyNumber.compute(ALICE, ALICE_KEY, BOB, "TUlUTS1LRVk="));
        assertNotEquals(original, SafetyNumber.compute(ALICE, "TUlUTS1LRVk=", BOB, BOB_KEY));
    }

    @Test
    public void bindsKeyToUserId() {
        // The same key claimed by a different user id must not produce the same fingerprint.
        assertNotEquals(SafetyNumber.fingerprint(ALICE, ALICE_KEY), SafetyNumber.fingerprint("eveUid", ALICE_KEY));
    }

    @Test
    public void formatsInGroupsOfFiveThreePerLine() {
        String number = "123456789012345678901234567890123456789012345678901234567890";

        assertEquals(
                "12345 67890 12345\n67890 12345 67890\n12345 67890 12345\n67890 12345 67890",
                SafetyNumber.format(number));
    }
}
