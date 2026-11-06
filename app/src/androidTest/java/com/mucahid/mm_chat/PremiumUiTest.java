package com.mucahid.mm_chat;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/** Exercises actual navigation and renders every redesigned layout without writing Firebase data. */
@RunWith(AndroidJUnit4.class)
public class PremiumUiTest {
    @Test public void registrationReturnsToLogin() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.create_account)).perform(scrollTo(), click());
            onView(withId(R.id.register_email)).check(matches(isDisplayed()));
            onView(withId(R.id.have_account)).perform(scrollTo(), click());
            onView(withId(R.id.login_email)).check(matches(isDisplayed()));
        }
    }

    @Test public void invalidPhoneDoesNotAdvanceAndCanBeEdited() {
        try (ActivityScenario<PhoneLoginActivity> scenario = ActivityScenario.launch(PhoneLoginActivity.class)) {
            onView(withId(R.id.phone_number_input)).perform(typeText("123"), closeSoftKeyboard());
            onView(withId(R.id.send_code_button)).perform(scrollTo(), click());
            onView(withId(R.id.phone_number_input)).check(matches(isDisplayed()));
            onView(withId(R.id.verify_code_button)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.change_phone_button)).perform(scrollTo(), click());
            onView(withId(R.id.phone_number_input)).check(matches(isDisplayed()));
        }
    }

    @Test public void renderAllScreens() throws Exception {
        int[] layouts = {R.layout.activity_login, R.layout.activity_register, R.layout.activity_phone_login,
                R.layout.activity_main, R.layout.activity_settings, R.layout.activity_profile,
                R.layout.activity_find_friends, R.layout.activity_chat, R.layout.activity_group_chat,
                R.layout.fragment_chats, R.layout.fragment_contacts, R.layout.fragment_groups,
                R.layout.fragment_requests, R.layout.item_user, R.layout.item_private_message_received,
                R.layout.item_private_message_sent, R.layout.item_group_message_received,
                R.layout.item_group_message_sent, R.layout.dialog_create_group};
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            for (int layout : layouts) {
                scenario.onActivity(activity -> {
                    activity.setContentView(layout);
                    TextView message = activity.findViewById(R.id.received_message);
                    if (message == null) message = activity.findViewById(R.id.sent_message);
                    if (message == null) message = activity.findViewById(R.id.message);
                    if (message != null) message.setText("Merhaba! Uzun mesajlar da rahatça okunmalı. Bugün nasılsın? Birlikte güzel bir sohbet edelim.");
                    TextView time = activity.findViewById(R.id.message_time);
                    if (time != null) time.setText("14:32");
                    TextView name = activity.findViewById(R.id.user_name);
                    if (name != null && !(name instanceof EditText)) name.setText("Deniz Yılmaz");
                    TextView status = activity.findViewById(R.id.user_status);
                    if (status != null) status.setText("Güzel bir gün, güzel bir sohbet.");
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                // Wait for the next drawn frame and the 220 ms window transition, not only the main looper.
                android.os.SystemClock.sleep(350);
                String name = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(layout);
                screenshot(name);
                scenario.onActivity(activity -> {
                    View content = activity.findViewById(android.R.id.content);
                    assertTrue("Layout must have a measured width", content.getWidth() > 0);
                    assertTrue("Layout must have a measured height", content.getHeight() > 0);
                });
            }
        }
    }

    private void screenshot(String name) throws Exception {
        File folder = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "ui-review");
        assertTrue(folder.isDirectory() || folder.mkdirs());
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try (FileOutputStream stream = new FileOutputStream(new File(folder, name + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        bitmap.recycle();
    }
}
