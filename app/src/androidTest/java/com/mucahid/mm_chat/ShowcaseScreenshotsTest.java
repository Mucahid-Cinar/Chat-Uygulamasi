package com.mucahid.mm_chat;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.mucahid.mm_chat.adapter.PrivateMessageAdapter;
import com.mucahid.mm_chat.adapter.UserViewHolder;
import com.mucahid.mm_chat.model.PrivateMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import static org.junit.Assert.*;

/** README fixtures: real layouts and adapters, fictional data, no Firebase writes. */
@RunWith(AndroidJUnit4.class)
public class ShowcaseScreenshotsTest {
    @Test public void captureGallery() throws Exception {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            capture("01-login");
            scenario.onActivity(a -> a.setContentView(R.layout.activity_register));
            capture("02-register");
            scenario.onActivity(a -> {
                a.setContentView(R.layout.activity_main);
                Toolbar toolbar = a.findViewById(R.id.main_toolbar);
                toolbar.setTitle("ME Chat");
                toolbar.setSubtitle("Sohbete yer aç.");
                toolbar.inflateMenu(R.menu.main_menu);
                TabLayout tabs = a.findViewById(R.id.main_tabs);
                String[] labels = {"Sohbetler", "Gruplar", "Kişiler", "İstekler"};
                int[] icons = {R.drawable.ic_baseline_message_24, R.drawable.ic_baseline_group_24,
                        R.drawable.ic_baseline_person_24, R.drawable.ic_baseline_notifications_active_24};
                for (int i = 0; i < labels.length; i++) tabs.addTab(tabs.newTab().setText(labels[i]).setIcon(icons[i]));
                ViewPager2 pager = a.findViewById(R.id.main_pager);
                pager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
                        View page = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chats, parent, false);
                        return new RecyclerView.ViewHolder(page) {};
                    }
                    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        holder.itemView.findViewById(R.id.list_state).setVisibility(View.GONE);
                        RecyclerView list = holder.itemView.findViewById(R.id.chats_list);
                        list.setLayoutManager(new LinearLayoutManager(a));
                        list.setAdapter(new RecyclerView.Adapter<UserViewHolder>() {
                            final String[] names = {"Deniz Yılmaz", "Ece Kaya", "Arda Demir", "Selin Aydın", "Mert Aksoy"};
                            @NonNull @Override public UserViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) { return UserViewHolder.create(p); }
                            @Override public void onBindViewHolder(@NonNull UserViewHolder h, int p) {
                                h.userName.setText(names[p]);
                                h.userStatus.setText(p < 2 ? "Çevrimiçi" : "Çevrimdışı");
                                h.userStatus.setTextColor(a.getResources().getColor(p < 2 ? R.color.blue : R.color.muted, a.getTheme()));
                            }
                            @Override public int getItemCount() { return names.length; }
                        });
                    }
                    @Override public int getItemCount() { return 1; }
                });
            });
            capture("03-conversations");
            scenario.onActivity(a -> {
                a.setContentView(R.layout.activity_chat);
                setText(a, R.id.profile_name, "Deniz Yılmaz");
                setText(a, R.id.profile_last_seen, "Çevrimiçi");
                a.findViewById(R.id.profile_last_seen_time).setVisibility(View.GONE);
                RecyclerView list = a.findViewById(R.id.private_messages_list);
                LinearLayoutManager manager = new LinearLayoutManager(a);
                manager.setStackFromEnd(true);
                list.setLayoutManager(manager);
                list.setAdapter(new PrivateMessageAdapter(Arrays.asList(
                        new PrivateMessage("deniz", "Merhaba! Bugün biraz yürüyüşe çıkalım mı?", "text", "14:30"),
                        new PrivateMessage("me", "Harika olur. Sahilde buluşalım mı?", "text", "14:31"),
                        new PrivateMessage("deniz", "Tam aklımdan geçen! Sonra da bir kahve içeriz.", "text", "14:31"),
                        new PrivateMessage("me", "Anlaştık, saat 17.00 gibi oradayım.", "text", "14:32"),
                        new PrivateMessage("deniz", "Görüşürüz! ☀", "text", "14:32")
                ), "me"));
            });
            capture("04-private-chat");
            scenario.onActivity(a -> {
                a.setContentView(R.layout.activity_settings);
                Toolbar toolbar = a.findViewById(R.id.settings_toolbar);
                toolbar.setTitle("Hesap Ayarları");
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
                setText(a, R.id.first_name, "Deniz");
                setText(a, R.id.last_name, "Yılmaz");
                setText(a, R.id.user_name, "denizyilmaz");
                setText(a, R.id.about_input, "Kahve, yolculuk ve güzel sohbetler.");
                a.findViewById(R.id.user_photo).setFocusableInTouchMode(true);
                a.findViewById(R.id.user_photo).requestFocus();
            });
            capture("05-settings");
            scenario.onActivity(a -> {
                a.setContentView(R.layout.activity_profile);
                Toolbar toolbar = a.findViewById(R.id.profile_toolbar);
                toolbar.setTitle("Profil");
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
                setText(a, R.id.user_name_display, "denizyilmaz");
                setText(a, R.id.user_first_name_display, "Deniz");
                setText(a, R.id.user_last_name_display, "Yılmaz");
                setText(a, R.id.user_status_display, "Kahve, yolculuk ve güzel sohbetler.");
            });
            capture("06-profile");
        }
    }

    private static void setText(LoginActivity activity, int id, String value) {
        ((TextView) activity.findViewById(id)).setText(value);
    }

    private void capture(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        android.os.SystemClock.sleep(400);
        File dir = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "showcase");
        assertTrue(dir.isDirectory() || dir.mkdirs());
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try (FileOutputStream output = new FileOutputStream(new File(dir, name + ".png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        }
        bitmap.recycle();
    }
}
