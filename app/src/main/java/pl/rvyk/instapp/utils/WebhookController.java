package pl.rvyk.instapp.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.rvyk.instapp.R;

public class WebhookController {
    public static void sendBugReportToWebhook(Throwable error, Context context) {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String webhookUrl = firebaseRemoteConfig.getString("DISCORD_WEBHOOK_URL");
                String bugReport = Utils.getStackTraceAsString(error);
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.POST, webhookUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(context, context.getResources().getString(R.string.reportSucess), Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, context.getResources().getString(R.string.reportFailure), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        JSONObject jsonBody = new JSONObject();
                        try {
                            JSONObject deviceInfoEmbed = new JSONObject();
                            deviceInfoEmbed.put("title", "Device Info");
                            deviceInfoEmbed.put("fields", createDeviceInfoFields(context));

                            JSONArray embedsArray = new JSONArray().put(deviceInfoEmbed);
                            jsonBody.put("embeds", embedsArray);

                            jsonBody.put("content", "```prolog\n " + bugReport + "```");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return jsonBody.toString().getBytes();
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                queue.add(request);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.reportFailure), Toast.LENGTH_SHORT).show();

            }
        });
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
            e.printStackTrace();
        }
        return "";
    }

}
