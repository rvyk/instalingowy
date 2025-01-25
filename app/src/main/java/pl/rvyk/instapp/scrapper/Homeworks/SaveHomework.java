package pl.rvyk.instapp.scrapper.Homeworks;

import okhttp3.*;
import pl.rvyk.instapp.scrapper.Scrapper;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class SaveHomework {
    private boolean success;

    public void save(String phpsessid, String link, String response, String action, Callback callback) throws IOException {

        try {

            URI uri = new URI(link);
            String student_id = getQueryParam(uri, "id");
            String homework_id = getQueryParam(uri, "homework_id");

            if (student_id != null && homework_id != null) {
                FormBody loginRequestBody = new FormBody.Builder()
                        .add("studentHomeworkID", homework_id)
                        .add("id", student_id)
                        .add("student_response", response)
                        .add(action, action.equals("save") ? "Zapisz" : "")
                        .build();

                Request request = new Request.Builder()
                        .url("https://instaling.pl/learning/editStudentHomework.php")
                        .addHeader("User-Agent", Scrapper.mozillaUserAgent)
                        .addHeader("Cookie", phpsessid)
                        .post(loginRequestBody)
                        .build();
                Scrapper.client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.header("Location") != null && Objects.requireNonNull(response.header("Location")).contains("homeworkPage.php")) {
                            success = true;
                            callback.onResponse(call, response);
                            return;
                        }
                        success = false;
                        callback.onResponse(call, response);
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        callback.onFailure(call, new IOException("[SaveHomework] -> Request failed"));
                    }
                });
            } else {
                throw new IOException("[SaveHomework] -> Cannot parse URI");
            }
        } catch (URISyntaxException e) {
            throw new IOException("[SaveHomework] -> URI parsing failed");
        }
    }

    private static String getQueryParam(URI uri, String paramName) {
        String query = uri.getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                if (paramName.equals(keyValue[0])) {
                    return keyValue.length > 1 ? keyValue[1] : null;
                }
            }
        }
        return null;
    }

    public boolean isSuccess() {
        return success;
    }

}
