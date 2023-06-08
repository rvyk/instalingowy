package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.IntentCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pl.rvyk.instapp.LoginActivity;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.UserInterface;
import pl.rvyk.instapp.utils.SnackbarController;
import pl.rvyk.instapp.utils.Utils;

public class Settings extends Fragment {

    Button changeLang, changeTheme, extraInfo;
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

                ViewGroup parent = (ViewGroup) languageDialog.getParent();
                if (parent != null) {
                    parent.removeView(languageDialog);
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setView(languageDialog);
                builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
                builder.show();
            }
        });

        changeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ViewGroup parent = (ViewGroup) themeDialog.getParent();
                if (parent != null) {
                    parent.removeView(themeDialog);
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setView(themeDialog);
                builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
                builder.show();
            }
        });

        RadioGroup themes = themeDialog.findViewById(R.id.theme_changer);

        SharedPreferences preferences = getActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int selectedTheme = preferences.getInt("SelectedTheme", R.id.system_theme);
        switch (selectedTheme) {
            case R.id.light_theme:
                themes.check(R.id.light_theme);
                break;
            case R.id.dark_theme:
                themes.check(R.id.dark_theme);
                break;
            default:
                themes.check(R.id.system_theme);
                break;
        }

        themes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final int selectedTheme = checkedId;

                FragmentActivity activity = requireActivity();

                SharedPreferences preferences = activity.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("SelectedTheme", selectedTheme);
                editor.apply();
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
                builder.setTitle(getResources().getString(R.string.settings_theme_restart_required_title));
                builder.setMessage(getResources().getString(R.string.settings_theme_restart_required));
                builder.setPositiveButton(getResources().getString(R.string.settings_theme_restart_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        getActivity().startActivity(intent);
                        getActivity().finishAffinity();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.settings_theme_restart_later), null);
                AlertDialog dialog = builder.create();
                dialog.setOwnerActivity(activity);
                dialog.show();
            }
        });

        extraInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ViewGroup parent = (ViewGroup) extraInfoDialog.getParent();
                if (parent != null) {
                    parent.removeView(extraInfoDialog);
                }

                try {
                    PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    String versionName = packageInfo.versionName;
                    int versionCode = packageInfo.versionCode;

                    TextView app_version, api_version, instaling_version;

                    app_version = extraInfoDialog.findViewById(R.id.app_version);
                    api_version = extraInfoDialog.findViewById(R.id.api_version);
                    instaling_version = extraInfoDialog.findViewById(R.id.instaling_version);

                    app_version.setText("VN: "+ versionName + ", VC: " + versionCode);

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
        });
        Button github, discord, crowdin, crowdinLangButton;

        github = extraInfoDialog.findViewById(R.id.githubRepo);
        discord = extraInfoDialog.findViewById(R.id.discordServer);
        crowdin = extraInfoDialog.findViewById(R.id.crowdinLink);
        crowdinLangButton = languageDialog.findViewById(R.id.langDialCrowBut);

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser("https://github.com/bettervulcan/instapp");
            }
        });
        discord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser("https://dc.ezinstaling.lol/");
            }
        });
        crowdin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser("https://crowdin.com/project/instapp");
            }
        });
        crowdinLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser("https://crowdin.com/project/instapp");
            }
        });
        return view;
    }
    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}