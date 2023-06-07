package pl.rvyk.instapp.utils;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import pl.rvyk.instapp.R;

public class SnackbarController {
    public static void showSnackbar(Context context, View linearLayout, Throwable error, String message, boolean details) {
        Snackbar snackbar = Snackbar.make(linearLayout, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        if (details) {
            snackbar.setAction(context.getResources().getString(R.string.snackbarDetails), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogWithError(error, context);
                }
            });
        }
        snackbar.setDuration(5000);
        snackbar.show();
    }

    private static void showDialogWithError(Throwable error, Context context) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(error.toString().split("\n")[0]);
        builder.setMessage(Utils.getStackTraceAsString(error));
        builder.setNegativeButton(context.getResources().getString(R.string.reportDialogCancel), null);
        builder.setPositiveButton(context.getResources().getString(R.string.reportDialogCopy), (dialog, which) -> {
            Utils.copyToClipboard(Utils.getStackTraceAsString(error), context);
            Toast.makeText(context, context.getResources().getString(R.string.reportDialogCopySuccess), Toast.LENGTH_SHORT).show();
        });
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isNetworkAvailable) {
            builder.setNeutralButton(context.getResources().getString(R.string.reportDialogReport), (dialog, which) -> {
                WebhookController.sendBugReportToWebhook(error, context);
            });
        }

        builder.show();
    }
}
