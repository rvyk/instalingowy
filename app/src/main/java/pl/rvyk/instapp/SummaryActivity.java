package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.rvyk.instapp.enums.Grades;

public class SummaryActivity extends AppCompatActivity {

    TextView answerStatus, readyAnswer, readyAnswerText;
    Button continueButton;
    LinearLayout summary;
    ProgressBar progressBar;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_page);

        answerStatus = findViewById(R.id.answerStatus);
        readyAnswer = findViewById(R.id.readyAnswer);
        readyAnswerText = findViewById(R.id.readyAnswerText);
        continueButton = findViewById(R.id.continueButton);
        summary = findViewById(R.id.summary);
        progressBar = findViewById(R.id.progressBar);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences = SummaryActivity.this.getSharedPreferences("Account1", Context.MODE_PRIVATE);

                String appid = sharedPreferences.getString("appid", "");
                String studentid = sharedPreferences.getString("studentid", "");
                String phpsessid = sharedPreferences.getString("phpsessid", "");

                Intent intent = new Intent(SummaryActivity.this, SolvingActivity.class);
                intent.putExtra("appid", appid);
                intent.putExtra("studentid", studentid);
                intent.putExtra("phpsessid", phpsessid);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            Integer grade = intent.getIntExtra("grade", 0);

            String word = intent.getStringExtra("word");
            String usageExample = intent.getStringExtra("usageExample");

            Grades grades = Grades.fromGrade(grade);
            String label = grades.getLabel(SummaryActivity.this);

            answerStatus.setText(label);
            readyAnswer.setText(word);
            readyAnswerText.setText(usageExample);

            progressBar.setVisibility(View.GONE);
            summary.setVisibility(View.VISIBLE);

        }
    }
}