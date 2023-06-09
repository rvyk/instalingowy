package pl.rvyk.instapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import pl.rvyk.instapp.databinding.ActivityUserinterfaceBinding;
import pl.rvyk.instapp.fragments.Homeworks;
import pl.rvyk.instapp.fragments.Settings;
import pl.rvyk.instapp.fragments.Start;
import pl.rvyk.instapp.utils.SnackbarController;

public class UserInterface extends AppCompatActivity {

    ActivityUserinterfaceBinding binding;

    ConstraintLayout content;

    TextView summaryWords, summaryDays, summaryUser;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserinterfaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        content = findViewById(R.id.userinterface);
        replaceFragment(new Start());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.fragmentHome:
                    replaceFragment(new Start());
                    break;
                case R.id.fragmentSettings:
                    replaceFragment(new Settings());
                    break;
                case R.id.fragmentHomeworks:
                    replaceFragment(new Homeworks());
                    break;
            }

            return true;
        });

        boolean showSummary = getIntent().getBooleanExtra("showSummary", false);

        if (showSummary) {
            try {
                LayoutInflater inflater = LayoutInflater.from(this);
                View summaryDialog = inflater.inflate(R.layout.summary_dialog, null);

                summaryWords = summaryDialog.findViewById(R.id.summaryWords);
                summaryDays = summaryDialog.findViewById(R.id.summaryWorkDays);
                summaryUser = summaryDialog.findViewById(R.id.summaryUser);

                Snackbar snackbar = Snackbar.make(content, getResources().getString(R.string.snackbar_session_finished), com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
                snackbar.setAction(getResources().getString(R.string.snackbar_session_finished_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPreferences = getSharedPreferences("Account1", Context.MODE_PRIVATE);
                        String login = sharedPreferences.getString("login", "");

                        summaryWords.setText(getResources().getString(R.string.summary_dialog_words) + " " + UserInterface.this.getIntent().getStringExtra("correct") + "/" + UserInterface.this.getIntent().getStringExtra("words"));
                        summaryUser.setText(getResources().getString(R.string.summary_dialog_for) + " " + login);
                        summaryDays.setText(getResources().getString(R.string.summary_dialog_working_days) + " " + UserInterface.this.getIntent().getStringExtra("instalingDays"));

                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(UserInterface.this);
                        builder.setView(summaryDialog);
                        builder.setNegativeButton(getResources().getString(R.string.reportDialogCancel), null);
                        builder.show();
                    }
                });
                snackbar.setDuration(10000);
                snackbar.show();
            } catch (Exception e) {
                SnackbarController.showSnackbar(UserInterface.this, content, e, getResources().getString(R.string.unkown_error), true);
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}

