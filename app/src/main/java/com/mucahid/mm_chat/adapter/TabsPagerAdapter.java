package com.mucahid.mm_chat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mucahid.mm_chat.ChatsFragment;
import com.mucahid.mm_chat.ContactsFragment;
import com.mucahid.mm_chat.GroupsFragment;
import com.mucahid.mm_chat.RequestsFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_CHATS = 0;
    public static final int TAB_GROUPS = 1;
    public static final int TAB_CONTACTS = 2;
    public static final int TAB_REQUESTS = 3;
    private static final int TAB_COUNT = 4;

    public TabsPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_GROUPS:
                return new GroupsFragment();
            case TAB_CONTACTS:
                return new ContactsFragment();
            case TAB_REQUESTS:
                return new RequestsFragment();
            case TAB_CHATS:
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
