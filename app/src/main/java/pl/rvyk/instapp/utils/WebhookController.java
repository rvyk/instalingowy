package pl.rvyk.instapp.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.rvyk.instapp.R;

public class WebhookController {
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendBugReportToWebhook(Throwable error, Context context) {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String webhookUrl = firebaseRemoteConfig.getString("DISCORD_WEBHOOK_URL");
                String bugReport = Utils.getStackTraceAsString(error);
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("username", "Instapp Bug Reporter");
                    jsonBody.put("content", "```" + bugReport + "```");

                    JSONObject embedObject = new JSONObject();
                    embedObject.put("title", "Device Info");
                    embedObject.put("color", 15548997);
                    embedObject.put("fields", createDeviceInfoFields(context));

                    JSONArray embedsArray = new JSONArray();
                    embedsArray.put(embedObject);
                    jsonBody.put("embeds", embedsArray);

                    RequestBody body = RequestBody.create(JSON, jsonBody.toString());
                    Request request = new Request.Builder()
                            .url(webhookUrl)
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            if (response.isSuccessful()) {
                                showToast(context, context.getResources().getString(R.string.reportSucess));
                            } else {
                                showToast(context, context.getResources().getString(R.string.reportFailure));
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("WebhookController", "Error while sending bug report to webhook", e);
                            showToast(context, context.getResources().getString(R.string.reportFailure));
                        }
                    });
                } catch (JSONException e) {
                    Log.e("WebhookController", "Error while creating JSON body", e);
                    showToast(context, context.getResources().getString(R.string.reportFailure));
                }
            } else {
                showToast(context, context.getResources().getString(R.string.reportFailure));
            }
        });
    }

    private static void showToast(Context context, String message) {
        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private static JSONArray createDeviceInfoFields(Context context) throws JSONException {
        JSONArray fieldsArray = new JSONArray();
        fieldsArray.put(createField("Device Model", Build.MODEL));
        fieldsArray.put(createField("Android Version", Build.VERSION.RELEASE));
        fieldsArray.put(createField("App Version", getAppVersion(context)));
        fieldsArray.put(createField("Date", Utils.getCurrentDate()));
        fieldsArray.put(createField("Time", Utils.getCurrentTime()));
        return fieldsArray;
    }

    private static JSONObject createField(String name, String value) throws JSONException {
        JSONObject fieldObject = new JSONObject();
        fieldObject.put("name", name);
        fieldObject.put("value", value);
        fieldObject.put("inline", true);
        return fieldObject;
    }

    private static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }
}
