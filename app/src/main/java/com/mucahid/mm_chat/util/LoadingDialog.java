package com.mucahid.mm_chat.util;

import android.content.Context;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

/** Replacement for the deprecated ProgressDialog. */
public final class LoadingDialog {

    private final Context context;
    private AlertDialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void show(String title, String message) {
        dismiss();
        ProgressBar progressBar = new ProgressBar(context);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        progressBar.setPadding(padding, padding, padding, padding);

        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(progressBar)
                .setCancelable(false)
                .create();
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
    }
}
