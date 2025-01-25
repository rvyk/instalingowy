package pl.rvyk.instapp.scrapper.GenerateQuestion;

import android.util.Log;
import okhttp3.*;
import pl.rvyk.instapp.scrapper.Scrapper;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

public class GenerateQuestion {
    private static final String TAG = "GenerateQuestion";
    private boolean success;
    private boolean ended;
    private int instalingDays;
    private int words;
    private int correct;
    private String question;
    private String questionID;
    private String generatedWord;

    public void generate(String phpSessionId, String appId, String studentId, Callback callback) {
        Log.d(TAG, "Starting question generation for student: " + studentId);

        String processedStudentId = studentId;
        if (studentId.contains("=")) {
            processedStudentId = studentId.split("=")[1];
            Log.d(TAG, "Processed student ID: " + processedStudentId);
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("child_id", processedStudentId)
                .add("date", String.valueOf(System.currentTimeMillis()))
                .build();

        Request request = new Request.Builder()
                .url("https://instaling.pl/ling2/server/actions/generate_next_word.php")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionId + "; " + appId)
                .post(requestBody)
                .build();

        Log.d(TAG, "Sending request to: " + request.url());
        
        Scrapper.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Received HTTP response: " + response.code());
                    assert response.body() != null;
                    String generatedOutput = response.body().string();
                    Log.d(TAG, "Server response: " + generatedOutput);

                    if (generatedOutput == null || generatedOutput.isEmpty()) {
                        Log.e(TAG, "Received empty response");
                        callback.onFailure(call, new IOException("[GenerateQuestion] -> Empty response"));
                        return;
                    }

                    JSONParser parser = new JSONParser();
                    try {
                        success = true;
                        JSONObject jsonObject = (JSONObject) parser.parse(generatedOutput);
                        Log.d(TAG, "Parsed JSON: " + jsonObject);
                        
                        Object idObj = jsonObject.get("id");
                        if (idObj != null) {
                            questionID = idObj.toString();
                            ended = false;
                            Log.d(TAG, "Found question ID: " + questionID);
                            
                            Object usageObj = jsonObject.get("usage_example");
                            Object translationsObj = jsonObject.get("translations");
                            
                            question = usageObj != null ? usageObj.toString() : "";
                            generatedWord = translationsObj != null ? translationsObj.toString() : "";
                            
                            Log.d(TAG, "Question: " + question);
                            Log.d(TAG, "Translation: " + generatedWord);
                            
                            callback.onResponse(call, response);
                            return;
                        }

                        try {
                            Log.d(TAG, "Attempting to parse progress information");
                            String summary = (String) jsonObject.get("summary");
                            if (summary != null) {
                                if (summary.contains("Dni pracy w tym tygodniu:")) {
                                    String daysStr = summary.split("Dni pracy w tym tygodniu:")[1].split("\\\\n")[0].trim();
                                    instalingDays = Integer.parseInt(daysStr);
                                    Log.d(TAG, "Found working days: " + instalingDays);
                                }

                                if (summary.contains("Powtórzyłeś")) {
                                    String statsStr = summary.split("Powtórzyłeś")[1].split("\\.")[0].trim();
                                    String[] parts = statsStr.split(",");
                                    
                                    String wordsStr = parts[0].trim().split(" ")[0].trim();
                                    words = Integer.parseInt(wordsStr);
                                    Log.d(TAG, "Found total words: " + words);
                                    
                                    if (parts.length > 1) {
                                        String correctStr = parts[1].trim().split(" ")[0].trim();
                                        correct = Integer.parseInt(correctStr);
                                        Log.d(TAG, "Found correct answers: " + correct);
                                    }
                                }
                                
                                Log.d(TAG, String.format("Progress: days=%d, words=%d, correct=%d", instalingDays, words, correct));
                            }
                            ended = true;
                            Log.d(TAG, "Session ended");
                            callback.onResponse(call, response);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing progress data: " + e.getMessage());
                            Log.e(TAG, "Stack trace: ", e);
                            callback.onFailure(call, new IOException("[GenerateQuestion] -> Error parsing progress data: " + e.getMessage()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                        callback.onFailure(call, new IOException("[GenerateQuestion] -> Error processing response: " + e.getMessage()));
                    }
                } else {
                    success = false;
                    Log.e(TAG, "Failed HTTP request: " + response.code());
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Request execution failed: " + e.getMessage());
                callback.onFailure(call, new IOException("[GenerateQuestion] -> Request failed"));
            }
        });
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isEnded() {
        return ended;
    }

    public int getInstalingDays() {
        return instalingDays;
    }

    public int getWords() {
        return words;
    }

    public int getCorrect() {
        return correct;
    }

    public String getQuestion() {
        return question;
    }

    public String getQuestionID() {
        return questionID;
    }

    public String getGeneratedWord() {
        return generatedWord;
    }

}
