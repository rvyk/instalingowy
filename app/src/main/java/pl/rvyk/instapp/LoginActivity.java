package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.color.DynamicColors;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.rvyk.instapp.scrapper.InstalingLogin.InstalingLogin;
import pl.rvyk.instapp.scrapper.Scrapper;
import pl.rvyk.instapp.utils.SnackbarController;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText instaLogin;
    private EditText instaPass;
    private Button loginBtn;
    private LinearLayout linearLayout;
    private LinearLayout mainLinearContent;
    private LinearLayout progressBar;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        initializeViews();

        SharedPreferences preferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int selectedTheme = preferences.getInt("SelectedTheme", R.id.system_theme);
        SharedPreferences langPreferences = getSharedPreferences("LanguagePrefs", MODE_PRIVATE);
        int selectedLanguage = langPreferences.getInt("SelectedLanguage", R.id.system_language);
        LocaleListCompat appLocale;

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

        switch (selectedLanguage) {
            case R.id.polish_language:
                appLocale = LocaleListCompat.forLanguageTags("pl-PL");
                AppCompatDelegate.setApplicationLocales(appLocale);
                break;
            case R.id.english_language:
                appLocale = LocaleListCompat.forLanguageTags("en-EN");
                AppCompatDelegate.setApplicationLocales(appLocale);
                break;
            case R.id.turkish_language:
                appLocale = LocaleListCompat.forLanguageTags("tr-TR");
                AppCompatDelegate.setApplicationLocales(appLocale);
                break;
            case R.id.ukrainian_language:
                appLocale = LocaleListCompat.forLanguageTags("uk-UA");
                AppCompatDelegate.setApplicationLocales(appLocale);
                break;
            default:
                appLocale = LocaleListCompat.forLanguageTags("system");
                AppCompatDelegate.setApplicationLocales(appLocale);
                break;
        }

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(KEY_LOGIN) && sharedPreferences.contains(KEY_PASSWORD)) {
            String savedLogin = sharedPreferences.getString(KEY_LOGIN, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            login(savedLogin, savedPassword);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        loginBtn.setOnClickListener(this);
    }

    private void initializeViews() {
        instaLogin = findViewById(R.id.instaLogin);
        instaPass = findViewById(R.id.instaPass);
        loginBtn = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.loginForm);
        mainLinearContent = findViewById(R.id.mainLinearContent);
    }

    private void login(String login, String password) {
        progressBar.setVisibility(View.VISIBLE);

        InstalingLogin instalingLogin = new InstalingLogin();
        instalingLogin.login(Scrapper.Methods.LOGIN_PASSWORD, login, password, null, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (instalingLogin.isSuccess()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_LOGIN, login);
                        editor.putString(KEY_PASSWORD, password);
                        editor.putString(KEY_PHPSESSID, instalingLogin.getPhpSessionID());
                        editor.putString(KEY_APPID, instalingLogin.getAppID());
                        editor.putString(KEY_STUDENTID, instalingLogin.getStudentID());
                        editor.putBoolean(KEY_SESSIONCOMPLETED, instalingLogin.isTodaySessionCompleted());
                        editor.putString(KEY_INSTALING_VERSION, instalingLogin.getInstalingVersion());
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, UserInterface.class);
                        startActivity(intent);
                        finish();
                    } else {
                        SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, null, getResources().getString(R.string.invalid_credentials), false);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    SnackbarController.showSnackbar(LoginActivity.this, mainLinearContent, e, getResources().getString(R.string.api_host_error), true);
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.loginButton) {
            String login = instaLogin.getText().toString();
            String password = instaPass.getText().toString();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            View keyboard = LoginActivity.this.getCurrentFocus();
            if (keyboard != null) {
                imm.hideSoftInputFromWindow(keyboard.getWindowToken(), 0);
            }

            login(login, password);
        }
    }
}
