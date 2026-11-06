package com.mucahid.mm_chat.util;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.mucahid.mm_chat.R;
import com.squareup.picasso.Picasso;

public final class ImageLoader {

    private ImageLoader() {
    }

    /** Loads a profile image, falling back to the default avatar when the url is missing. */
    public static void loadProfileImage(@Nullable String url, ImageView target) {
        if (url == null || url.isEmpty()) {
            Picasso.get().cancelRequest(target);
            target.setImageResource(R.drawable.ic_avatar);
            return;
        }
        Picasso.get()
                .load(url)
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .into(target);
    }
}
