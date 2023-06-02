package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pl.rvyk.instapp.solving.GenerateQuestionRequestHelper;
import pl.rvyk.instapp.solving.SendAnswerRequestHelper;

public class SolvingActivity extends AppCompatActivity {

    private TextView questionTextView;

    TextView translations, translateText;
    EditText answer;
    Button sendAnswerButton;
    ProgressBar progressBar;
    LinearLayout sendAnswer, summary;
    private Button viewById;
    private String questionIdentificator;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solving_page);
        refreshContent();
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        refreshContent();
    }

    private void refreshContent() {
        translations = findViewById(R.id.translations);
        translateText = findViewById(R.id.translateText);
        progressBar = findViewById(R.id.progressBar);
        sendAnswer = findViewById(R.id.sendAnswer);
        summary = findViewById(R.id.summary);
        sendAnswerButton = findViewById(R.id.sendAnswerButton);
        answer = findViewById(R.id.answer);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String phpSessionId = bundle.getString("phpsessid");
            String appId = bundle.getString("appid");
            String studentId = bundle.getString("studentid");

            sendAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String answered = answer.getText().toString();
                    sharedPreferences = SolvingActivity.this.getSharedPreferences("Account1", Context.MODE_PRIVATE);

                    String login = sharedPreferences.getString("login", "");
                    SendAnswerRequestHelper.sendAnswerRequest(SolvingActivity.this, phpSessionId, appId, studentId, questionIdentificator, answered, login, new SendAnswerRequestHelper.SendAnswerResponseListener() {
                        @Override
                        public void onSuccess(Integer grade, String word, String usageExample) {
                            Intent intent = new Intent(SolvingActivity.this, SummaryActivity.class);
                            intent.putExtra("grade", grade);
                            intent.putExtra("word", word);
                            intent.putExtra("usageExample", usageExample);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(SolvingActivity.this, "Wystąpił błąd" + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            GenerateQuestionRequestHelper.sendGenerateQuestionRequest(this, phpSessionId, appId, studentId, new GenerateQuestionRequestHelper.GenerateQuestionResponseListener() {
                @Override
                public void onSuccess(String question, String usageExample, String questionId) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            sendAnswer.setVisibility(View.VISIBLE);
                            questionIdentificator = questionId;
                            translations.setText(question);
                            translateText.setText(usageExample);
                        }
                    });
                }
                @Override
                public void onFinish(boolean ended) {
                    Toast.makeText(SolvingActivity.this, "Sesja zakończona", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SolvingActivity.this, UserInterface.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Obsługa błędu
                        }
                    });
                }
            });
        }
    }
}
