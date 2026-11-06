package com.mucahid.mm_chat.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageActivity;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

/**
 * Opens the image source chooser + cropper. The library's own CropImageContract is deprecated
 * and its maintainers ask apps to provide their own contract; this one mirrors it.
 */
public class CropImageResultContract extends ActivityResultContract<CropImageOptions, CropImageView.CropResult> {

    @NonNull
    @Override
    @SuppressWarnings("deprecation") // the library asks apps to eventually ship their own crop Activity
    public Intent createIntent(@NonNull Context context, CropImageOptions options) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, null);
        bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
        return new Intent(context, CropImageActivity.class)
                .putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
    }

    @NonNull
    @Override
    @SuppressWarnings("deprecation") // typed getParcelableExtra requires API 33
    public CropImageView.CropResult parseResult(int resultCode, @Nullable Intent intent) {
        CropImage.ActivityResult result = intent == null ? null
                : intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_RESULT);
        if (result == null || resultCode == Activity.RESULT_CANCELED) {
            return CropImage.CancelledResult.INSTANCE;
        }
        return result;
    }
}
