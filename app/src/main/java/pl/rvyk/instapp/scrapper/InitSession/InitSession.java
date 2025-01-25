package pl.rvyk.instapp.scrapper.InitSession;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import pl.rvyk.instapp.scrapper.Scrapper;

import java.io.IOException;

public class InitSession {
    private boolean success;

    public void init(String phpSessionId, String appId, String studentId, Callback callback) {
        String processedStudentId = studentId;
        if (studentId.contains("=")) {
            processedStudentId = studentId.split("=")[1];
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("child_id", processedStudentId)
                .add("repeat", "")
                .add("start", "")
                .add("end", "")
                .build();
        Request request = new Request.Builder()
                .url("https://instaling.pl/ling2/server/actions/init_session.php")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionId + "; " + appId)
                .post(requestBody)
                .build();
        Scrapper.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[InitSession] -> Request failed"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                success = response.isSuccessful();
                callback.onResponse(call, response);
            }
        });
    }

    public boolean isSuccess() {
        return success;
    }
}
