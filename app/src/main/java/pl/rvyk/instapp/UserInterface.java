package pl.rvyk.instapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import pl.rvyk.instapp.utils.SnackbarController;
import pl.rvyk.instapp.utils.Utils;
import pl.rvyk.instapp.utils.WebhookController;

public class UserInterface extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    PageAdapter pageAdapter;
    RelativeLayout content;

    TextView summaryWords, summaryDays, summaryUser;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinterface);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        content = findViewById(R.id.userinterface);

        pageAdapter = new PageAdapter(this);
        viewPager2.setAdapter(pageAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
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

                        summaryWords.setText("Wykonane słówka: " + UserInterface.this.getIntent().getStringExtra("correct") + "/" + UserInterface.this.getIntent().getStringExtra("words"));
                        summaryUser.setText(login);
                        summaryDays.setText("Ilość dni pracy: " + UserInterface.this.getIntent().getStringExtra("instalingDays"));

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
}

