package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText instaLogin, instaPass;
    Button loginBtn;
    ProgressBar progressBar;
    TextView loginTitle;
    LinearLayout linearLayout;
    private static final String SHARED_PREF_NAME = "Account1";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHPSESSID = "phpsessid";
    private static final String KEY_APPID = "appid";
    private static final String KEY_STUDENTID = "studentid";
    private static final String KEY_BUTTONTEXT = "buttontext";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        instaLogin = findViewById(R.id.instaLogin);
        instaPass = findViewById(R.id.instaPass);
        loginBtn = findViewById(R.id.loginin);
        progressBar = findViewById(R.id.progressBar);
        loginTitle = findViewById(R.id.loginTitle);
        linearLayout = findViewById(R.id.loginForm);

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
        } catch (JSONException e) {
            e.printStackTrace();
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
                                Toast.makeText(MainActivity.this, "Zalogowano pomyślnie", Toast.LENGTH_SHORT).show();

                                // Zapisywanie danych logowania do SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_LOGIN, login);
                                editor.putString(KEY_PASSWORD, password);
                                editor.putString(KEY_PHPSESSID, response.getString("phpsessid"));
                                editor.putString(KEY_APPID, response.getString(("appid")));
                                editor.putString(KEY_STUDENTID, response.getString("studentid"));
                                editor.putString(KEY_BUTTONTEXT, response.getString("buttonText"));
                                editor.apply();

                                Intent intent = new Intent(MainActivity.this, UserInterface.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Błąd logowania", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                linearLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Błąd komunikacji z serwerem", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
        );
        Volley.newRequestQueue(MainActivity.this).add(request);
    }
}

