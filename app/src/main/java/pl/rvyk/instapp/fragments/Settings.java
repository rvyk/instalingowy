package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pl.rvyk.instapp.R;
import pl.rvyk.instapp.UserInterface;
import pl.rvyk.instapp.utils.SnackbarController;

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
        Button github, discord;

        github = extraInfoDialog.findViewById(R.id.githubRepo);
        discord = extraInfoDialog.findViewById(R.id.discordServer);

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
        return view;
    }
    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}