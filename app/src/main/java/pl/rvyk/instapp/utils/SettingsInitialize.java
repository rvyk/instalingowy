package pl.rvyk.instapp.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import pl.rvyk.instapp.R;

public class SettingsInitialize {

    public static void Initialize(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int selectedTheme = preferences.getInt("SelectedTheme", R.id.system_theme);
        SharedPreferences langPreferences = context.getSharedPreferences("LanguagePrefs", MODE_PRIVATE);
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
    }
}
