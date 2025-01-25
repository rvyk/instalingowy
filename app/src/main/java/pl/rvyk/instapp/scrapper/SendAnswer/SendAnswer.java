package pl.rvyk.instapp.scrapper.SendAnswer;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pl.rvyk.instapp.scrapper.Scrapper;

import java.io.IOException;

public class SendAnswer {

    private boolean success;
    private Long grade;
    private String word;
    private String question;

    public void send(String phpSessionId, String appId, String studentId, String questionID, String answer, Callback callback) {

        String processedStudentId = studentId;
        if (studentId.contains("=")) {
            processedStudentId = studentId.split("=")[1];
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("child_id", processedStudentId)
                .add("answer", answer)
                .add("word_id", questionID)
                .add("version", "C65E24B29F60B1231EC23D979C9707D2")
                .build();

        Request request = new Request.Builder()
                .url("https://instaling.pl/ling2/server/actions/save_answer.php")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionId + "; " + appId)
                .post(requestBody)
                .build();

        Scrapper.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String generatedOutput = response.body().string();
                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject jsonObject = (JSONObject) parser.parse(generatedOutput);
                        success = true;
                        grade = (Long) jsonObject.get("grade");
                        word = (String) jsonObject.get("word");
                        question = (String) jsonObject.get("usage_example");
                        callback.onResponse(call, response);
                    } catch (Exception e) {
                        callback.onFailure(call, new IOException("[SendAnswer] -> Error processing response"));
                    } finally {
                        response.body().close();
                    }
                } else {
                    success = false;
                    callback.onResponse(call, response);
                    response.body().close();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[SendAnswer] -> Request failed"));
            }
        });

    }

    public boolean isSuccess() {
        return success;
    }

    public String getQuestion() {
        return question;
    }

    public Long getGrade() {
        return grade;
    }

    public String getWord() {
        return word;
    }
}
