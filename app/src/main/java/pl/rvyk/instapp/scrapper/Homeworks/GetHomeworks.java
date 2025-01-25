package pl.rvyk.instapp.scrapper.Homeworks;

import okhttp3.*;
import pl.rvyk.instapp.scrapper.Scrapper;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetHomeworks {
    private HomeworkResult result;

    public void getHomeworks(String phpSessionId, String appId, String studentId, Callback callback) {
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
                .url("https://instaling.pl/learning/choose_homework.php?id=" + processedStudentId + "&list=true")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionId + "; " + appId)
                .post(requestBody)
                .build();
        Scrapper.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[GetHomeworks] -> Request failed"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                result = new HomeworkResult();
                if (response.isSuccessful()) {
                    Document root = Jsoup.parse(response.body().string());
                    Elements tableRows = root.select(".homework_table_row");

                    result.setSuccess(true);
                    result.setHomeworksDone(new ArrayList<>());
                    result.setHomeworksTodo(new ArrayList<>());
                    for (Element row : tableRows) {
                        String title = row.select("td:nth-child(1) a").text().trim();
                        String deadline = row.select("td:nth-child(2)").text();
                        String homeworkLink = row.select("td:nth-child(1) a").attr("href");
                        Element gradeCell = row.select("td:nth-last-child(1)").first();
                        if (gradeCell.select("div").first() == null && gradeCell.text() != null) {
                            String grade = gradeCell.text();
                            if (grade.isEmpty()) {
                                grade = "Brak oceny";
                            }

                            HomeworkItem homeworkDone = new HomeworkItem();
                            homeworkDone.setTitle(title);
                            homeworkDone.setDeadline(deadline);
                            homeworkDone.setHomeworkLink(homeworkLink);
                            homeworkDone.setGrade(grade);

                            result.getHomeworksDone().add(homeworkDone);
                        } else {
                            HomeworkItem homeworkTodo = new HomeworkItem();
                            homeworkTodo.setTitle(title);
                            homeworkTodo.setDeadline(deadline);
                            homeworkTodo.setHomeworkLink(homeworkLink);

                            result.getHomeworksTodo().add(homeworkTodo);
                        }
                    }
                    callback.onResponse(call, response);
                } else {
                    result.setSuccess(false);
                    callback.onResponse(call, response);
                }
            }
        });
    }

    public static class HomeworkResult {
        private boolean success;
        private List<HomeworkItem> homeworksTodo;
        private List<HomeworkItem> homeworksDone;

        public HomeworkResult() {
        }

        public HomeworkResult(boolean success, List<HomeworkItem> homeworksTodo, List<HomeworkItem> homeworksDone) {
            this.success = success;
            this.homeworksTodo = homeworksTodo;
            this.homeworksDone = homeworksDone;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<HomeworkItem> getHomeworksTodo() {
            return homeworksTodo;
        }

        public void setHomeworksTodo(List<HomeworkItem> homeworksTodo) {
            this.homeworksTodo = homeworksTodo;
        }

        public List<HomeworkItem> getHomeworksDone() {
            return homeworksDone;
        }

        public void setHomeworksDone(List<HomeworkItem> homeworksDone) {
            this.homeworksDone = homeworksDone;
        }
    }

    public HomeworkResult getResult() {
        return result;
    }

}
