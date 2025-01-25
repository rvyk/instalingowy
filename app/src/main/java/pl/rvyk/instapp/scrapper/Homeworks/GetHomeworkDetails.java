package pl.rvyk.instapp.scrapper.Homeworks;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import pl.rvyk.instapp.scrapper.Scrapper;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class GetHomeworkDetails {
    private boolean success;
    private String title;
    private String deadline;

    private String exercise;
    private String ytLink;
    private String answer;
    private String note;
    private String grade;
    private boolean done;


    public void details(String phpsessid, String link, Callback callback) {
        Request request = new Request.Builder()
                .url("https://instaling.pl/learning/" + link)
                .addHeader("User-Agent", Scrapper.mozillaUserAgent)
                .addHeader("Cookie", phpsessid)
                .build();
        Scrapper.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                if (response.isSuccessful()) {
                    Document root = Jsoup.parse(response.body().string());
                    title = root.select("h3").text().trim().split(":")[0];
                    deadline = root.select("h5").text().trim().split("Termin: ")[1];
                    exercise = root.select("textarea[name=\"exercise\"]").text().trim();
                    ytLink = root.select("iframe.youtube-film").attr("src").trim();
                    note = root.select("textarea[name=\"teacher_response\"]").attr("placeholder").trim();
                    answer = root.select("textarea[name=\"student_response\"]").text().trim();
                    String gradeElement = root.select("input[name=\"grade\"]").attr("value").trim();
                    grade = !gradeElement.isEmpty() ? gradeElement : "Brak oceny";
                    done = root.select("#account_page > div.alert.alert-error").text().isEmpty();
                    success = true;
                    callback.onResponse(call, response);
                } else {
                    success = false;
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[GetHomeworkDetails] -> Request failed"));
            }
        });
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTitle() {
        return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getExercise() {
        return exercise;
    }

    public String getYtLink() {
        return ytLink;
    }

    public String getAnswer() {
        return answer;
    }

    public String getNote() {
        return note;
    }

    public String getGrade() {
        return grade;
    }

    public boolean isDone() {
        return done;
    }
}
