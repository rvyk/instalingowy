package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import pl.rvyk.instapp.utils.SnackbarController;

public class LoginActivity extends AppCompatActivity{

    EditText instaLogin, instaPass;
    Button loginBtn;
    ProgressBar progressBar;
    LinearLayout linearLayout, mainLinearContent;
    private static final String SHARED_PREF_NAME = "Account1";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHPSESSID = "phpsessid";
    private static final String KEY_APPID = "appid";
    private static final String KEY_STUDENTID = "studentid";
    private static final String KEY_SESSIONCOMPLETED = "todaySessionCompleted";
    private static final String KEY_INSTALING_VERSION = "instalingVersion";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int selectedTheme = preferences.getInt("SelectedTheme", R.id.system_theme);

        Log.d("selectedTheme", String.valueOf(selectedTheme));
        switch (selectedTheme) {
            case R.id.light_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.dark_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        instaLogin = findViewById(R.id.instaLogin);
        instaPass = findViewById(R.id.instaPass);
        loginBtn = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.loginForm);
        mainLinearContent = findViewById(R.id.mainLinearContent);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(KEY_LOGIN) && sharedPreferences.contains(KEY_PASSWORD)) {
            String savedLogin = sharedPreferences.getString(KEY_LOGIN, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            login(savedLogin, savedPassword);
        } else {
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = instaLogin.getText().toString();
                String password = instaPass.getText().toString();

                login(login, password);

            }
        });
    }

    private void login(final String login, final String password) {
        progressBar.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("login", login);
            jsonBody.put("password", password);
        } catch (JSONException error) {
            SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, error, getResources().getString(R.string.unkown_error), true);
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://api.ezinstaling.lol/api/v1/instaling/checkaccount",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_LOGIN, login);
                                editor.putString(KEY_PASSWORD, password);
                                editor.putString(KEY_PHPSESSID, response.getString("phpsessid"));
                                editor.putString(KEY_APPID, response.getString("appid"));
                                editor.putString(KEY_STUDENTID, response.getString("studentid"));
                                editor.putBoolean(KEY_SESSIONCOMPLETED, response.getBoolean("todaySessionCompleted"));
                                editor.putString(KEY_INSTALING_VERSION, response.getString("instalingVersion"));
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, UserInterface.class);
                                startActivity(intent);
                                finish();
                            } else {
                                SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, null, getResources().getString(R.string.invalid_credentials), false);
                                progressBar.setVisibility(View.GONE);
                                linearLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException error) {
                            SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, error, getResources().getString(R.string.unkown_error), true);
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                            SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, null, getResources().getString(R.string.invalid_credentials), false);
                        } else {
                            SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, error, getResources().getString(R.string.api_host_error), true);
                        }
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
        );
        Volley.newRequestQueue(LoginActivity.this).add(request);
    }

}
