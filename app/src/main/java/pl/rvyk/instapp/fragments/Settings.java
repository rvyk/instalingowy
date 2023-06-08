package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pl.rvyk.instapp.LoginActivity;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.utils.SnackbarController;

public class Settings extends Fragment {

    private Button changeLang;
    private Button changeTheme;
    private Button extraInfo;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        View languageDialog = inflater.inflate(R.layout.language_dialog, null);
        View themeDialog = inflater.inflate(R.layout.theme_dialog, null);
        View extraInfoDialog = inflater.inflate(R.layout.extra_info_dialog, null);

        changeLang = view.findViewById(R.id.chooseLang);
        changeTheme = view.findViewById(R.id.chooseTheme);
        extraInfo = view.findViewById(R.id.moreinfo);

        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLanguageDialog(languageDialog);
            }
        });

        RadioGroup languages = languageDialog.findViewById(R.id.language_changer);
        setLanguageSelection(languages);

        languages.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveSelectedLanguage(checkedId);
                restartRequired();
            }
        });

        changeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThemeDialog(themeDialog);
            }
        });

        RadioGroup themes = themeDialog.findViewById(R.id.theme_changer);
        setThemeSelection(themes);

        themes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveSelectedTheme(checkedId);
                restartRequired();
            }
        });

        extraInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraInfoDialog(extraInfoDialog, view);
            }
        });

        Button github = extraInfoDialog.findViewById(R.id.githubRepo);
        Button discord = extraInfoDialog.findViewById(R.id.discordServer);
        Button crowdin = extraInfoDialog.findViewById(R.id.crowdinLink);
        Button crowdinLangButton = languageDialog.findViewById(R.id.langDialCrowBut);

        setButtonClickListener(github, "https://github.com/bettervulcan/instapp");
        setButtonClickListener(discord, "https://dc.ezinstaling.lol/");
        setButtonClickListener(crowdin, "https://crowdin.com/project/instapp");
        setButtonClickListener(crowdinLangButton, "https://crowdin.com/project/instapp");

        return view;
    }

    private void showLanguageDialog(View languageDialog) {
        ViewGroup parent = (ViewGroup) languageDialog.getParent();
        if (parent != null) {
            parent.removeView(languageDialog);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setView(languageDialog);
        builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
        builder.show();
    }

    private void setLanguageSelection(RadioGroup languages) {
        SharedPreferences langPreferences = getActivity().getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        int selectedLanguage = langPreferences.getInt("SelectedLanguage", R.id.system_language);
        languages.check(selectedLanguage);
    }

    private void saveSelectedLanguage(int selectedLanguage) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SelectedLanguage", selectedLanguage);
        editor.apply();
    }

    private void showThemeDialog(View themeDialog) {
        ViewGroup parent = (ViewGroup) themeDialog.getParent();
        if (parent != null) {
            parent.removeView(themeDialog);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setView(themeDialog);
        builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
        builder.show();
    }

    private void setThemeSelection(RadioGroup themes) {
        SharedPreferences preferences = getActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int selectedTheme = preferences.getInt("SelectedTheme", R.id.system_theme);
        themes.check(selectedTheme);
    }

    private void saveSelectedTheme(int selectedTheme) {
        FragmentActivity activity = requireActivity();

        SharedPreferences preferences = activity.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SelectedTheme", selectedTheme);
        editor.apply();
    }

    private void restartRequired() {
        FragmentActivity activity = requireActivity();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(getResources().getString(R.string.settings_restart_required_title));
        builder.setMessage(getResources().getString(R.string.settings_theme_restart_required));
        builder.setPositiveButton(getResources().getString(R.string.settings_restart_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartApplication();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.settings_restart_later), null);
        AlertDialog dialog = builder.create();
        dialog.setOwnerActivity(activity);
        dialog.show();
    }

    private void restartApplication() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        getActivity().startActivity(intent);
        getActivity().finishAffinity();
    }

    private void showExtraInfoDialog(View extraInfoDialog, View view) {
        ViewGroup parent = (ViewGroup) extraInfoDialog.getParent();
        if (parent != null) {
            parent.removeView(extraInfoDialog);
        }

        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            TextView app_version = extraInfoDialog.findViewById(R.id.app_version);
            TextView api_version = extraInfoDialog.findViewById(R.id.api_version);
            TextView instaling_version = extraInfoDialog.findViewById(R.id.instaling_version);

            app_version.setText("VN: " + versionName + ", VC: " + versionCode);

            VersionFetcher.fetchAPIVersion(getActivity(), new VersionFetcher.FetchAPIVersionListener() {
                @Override
                public void onSuccess(String version) {
                    api_version.setText(version);
                }

                @Override
                public void onError(Throwable error) {
                    SnackbarController.showSnackbar(getActivity(), view, error, getResources().getString(R.string.unkown_error), true);
                    api_version.setText(getResources().getString(R.string.unkown_error));
                }
            });

            sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);
            String instalingVersion = sharedPreferences.getString("instalingVersion", "");
            instaling_version.setText(instalingVersion);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setView(extraInfoDialog);
            builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
            builder.show();
        } catch (Exception error) {
            SnackbarController.showSnackbar(getActivity(), view, error, getResources().getString(R.string.unkown_error), true);
        }
    }

    private void setButtonClickListener(Button button, final String url) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser(url);
            }
        });
    }

    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
