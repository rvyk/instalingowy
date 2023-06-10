package pl.rvyk.instapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.rvyk.instapp.solving.GenerateQuestionRequestHelper;
import pl.rvyk.instapp.solving.SendAnswerRequestHelper;
import pl.rvyk.instapp.utils.SnackbarController;

public class SolvingActivity extends AppCompatActivity {

    TextView translations, translateText;
    EditText answer;
    Button sendAnswerButton, refreshButton;
    LinearLayout sendAnswer, mainLinearContent, noConnection, progressBar;
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
        sendAnswerButton = findViewById(R.id.sendAnswerButton);
        answer = findViewById(R.id.answer);
        mainLinearContent = findViewById(R.id.solving_page);
        noConnection = findViewById(R.id.noConnection);
        refreshButton = findViewById(R.id.refresh);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isNetworkAvailable) {
            noConnection.setVisibility(View.VISIBLE);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refreshContent();
                }
            });
        }

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
                        public void onError(Throwable error) {
                            Log.d("err", error.toString());
                            SnackbarController.showSnackbar(SolvingActivity.this, mainLinearContent, error, getResources().getString(R.string.unkown_error), true);
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
                            questionIdentificator = questionId;
                            translations.setText(question);
                            translateText.setText(usageExample);
                        }
                    });
                }
                @Override
                public void onFinish(boolean ended, String instalingDays, String words, String correct) {
                    SnackbarController.showSnackbar(SolvingActivity.this, mainLinearContent, null, getResources().getString(R.string.solving_finished), false);
                    Intent intent = new Intent(SolvingActivity.this, UserInterface.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("showSummary", true);
                    intent.putExtra("instalingDays", instalingDays);
                    intent.putExtra("words", words);
                    intent.putExtra("correct", correct);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onError(Throwable error) {
                    SnackbarController.showSnackbar(SolvingActivity.this, mainLinearContent, error, getResources().getString(R.string.unkown_error), true);
                }
            });
        }
    }
}
