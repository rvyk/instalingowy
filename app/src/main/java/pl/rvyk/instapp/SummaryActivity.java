package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.rvyk.instapp.enums.Grades;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView answerStatus;
    private TextView readyAnswer;
    private TextView readyAnswerText;
    private Button continueButton;
    private LinearLayout progressBar;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_page);

        initializeViews();
        setClickListeners();

        Intent intent = getIntent();
        if (intent != null) {
            int grade = intent.getIntExtra("grade", 0);
            String word = intent.getStringExtra("word");
            String usageExample = intent.getStringExtra("usageExample");

            Grades grades = Grades.fromGrade(grade);
            String label = grades.getLabel(this);

            answerStatus.setText(label);
            readyAnswer.setText(word);
            readyAnswerText.setText(usageExample);

            progressBar.setVisibility(View.GONE);
        }
    }

    private void initializeViews() {
        answerStatus = findViewById(R.id.answerStatus);
        readyAnswer = findViewById(R.id.readyAnswer);
        readyAnswerText = findViewById(R.id.readyAnswerText);
        continueButton = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setClickListeners() {
        continueButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        sharedPreferences = getSharedPreferences("Account1", Context.MODE_PRIVATE);

        String appid = sharedPreferences.getString("appid", "");
        String studentid = sharedPreferences.getString("studentid", "");
        String phpsessid = sharedPreferences.getString("phpsessid", "");

        Intent intent = new Intent(this, SolvingActivity.class);
        intent.putExtra("appid", appid);
        intent.putExtra("studentid", studentid);
        intent.putExtra("phpsessid", phpsessid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
