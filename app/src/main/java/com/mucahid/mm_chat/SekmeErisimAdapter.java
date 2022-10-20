package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SekmeErisimAdapter extends FragmentPagerAdapter {
    public SekmeErisimAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {

        switch (position){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GrupFragment grupFragment = new GrupFragment();
                return grupFragment;
            case 2:
                KisilerFragment kisilerFragment = new KisilerFragment();
                return kisilerFragment;
            case 3:
                IsteklerFragment isteklerFragment = new IsteklerFragment();
                return isteklerFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}


