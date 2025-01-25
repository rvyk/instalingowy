package pl.rvyk.instapp.scrapper.InstalingLogin;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.rvyk.instapp.scrapper.Scrapper;
import pl.rvyk.instapp.scrapper.SessionValidation.SessionValidation;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class InstalingLogin {
    private boolean success;
    private boolean todaySessionCompleted;
    private String message;
    private String studentID;
    private String buttonText;
    private String instalingVersion;
    private String phpSessionID;
    private String appID;
    private int homeworkCount = 0;

    public void login(Scrapper.Methods loginMethod, String email, String password, String phpsessid, Callback callback) {
        if (loginMethod.equals(Scrapper.Methods.LOGIN_PASSWORD)) {
            Request loginRequest = createLoginRequest(email, password);
            Scrapper.client.newCall(loginRequest).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response loginResponse) throws IOException {
                    handleLoginResponse(call, loginResponse, callback);
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFailure(call, new IOException("[InstalingLogin] -> Login request failed"));
                }
            });

        } else {
            phpSessionID = phpsessid;
            Request loginRequest = createDispatcherRequest();
            Scrapper.client.newCall(loginRequest).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    SessionValidation sessionValidation = new SessionValidation();
                    sessionValidation.validateSesion(phpSessionID, new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (sessionValidation.isSuccess()) {
                                handleDispatcherResponse(call, response, callback);
                            } else {
                                success = false;
                                message = "Login Failed";
                                callback.onResponse(call, response);
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            callback.onFailure(call, new IOException("[InstalingLogin] -> Error while validating PHPSessionID"));
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFailure(call, new IOException("[InstalingLogin] -> Dispatcher request failed"));
                }
            });
        }
    }

    private Request createLoginRequest(String email, String password) {
        FormBody loginRequestBody = new FormBody.Builder()
                .add("action", "login")
                .add("log_email", email)
                .add("log_password", password)
                .build();
        return new Request.Builder()
                .url("https://instaling.pl/teacher.php?page=teacherActions")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .post(loginRequestBody)
                .build();
    }

    private void handleLoginResponse(Call call, Response loginResponse, Callback callback) throws IOException {
        try {
            if (!Objects.requireNonNull(loginResponse.header("Location")).startsWith("learning/dispatcher")) {
                success = false;
                message = "Login Failed";
                callback.onResponse(call, loginResponse);
                return;
            }
            List<String> setCookieHeaders = loginResponse.headers("Set-Cookie");
            phpSessionID = setCookieHeaders.get(0).split(";")[0];
            Request dispatcherRequest = createDispatcherRequest();
            Scrapper.client.newCall(dispatcherRequest).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response dispatcherResponse) {
                    try {
                        handleDispatcherResponse(call, dispatcherResponse, callback);
                    } finally {
                        if (dispatcherResponse.body() != null) {
                            dispatcherResponse.body().close();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFailure(call, new IOException("[InstalingLogin] -> Dispatcher request failed"));
                }
            });
        } finally {
            if (loginResponse.body() != null) {
                loginResponse.body().close();
            }
        }
    }

    private Request createDispatcherRequest() {
        return new Request.Builder()
                .url("https://instaling.pl/learning/dispatcher.php")
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionID)
                .build();
    }

    private void handleDispatcherResponse(Call ignored, Response dispatcherResponse, Callback callback) {
        success = true;
        message = "Login Successfully";
        appID = "app=app_84";
        dispatcherResponse.headers("Set-Cookie");
        appID = Objects.requireNonNull(dispatcherResponse.header("Set-Cookie")).split(";")[0];
        studentID = Objects.requireNonNull(dispatcherResponse.header("Location")).split("\\?")[1];
        Request mainPageRequest = createMainPageRequest();
        Scrapper.client.newCall(mainPageRequest).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response mainPageResponse) throws IOException {
                handleMainPageResponse(call, mainPageResponse, callback);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[InstalingLogin] -> Final request failed"));
            }
        });
    }

    private Request createMainPageRequest() {
        return new Request.Builder()
                .url("https://instaling.pl/student/pages/mainPage.php?" + studentID)
                .header("User-Agent", Scrapper.mozillaUserAgent)
                .header("Cookie", phpSessionID + "; ")
                .build();
    }

    private void handleMainPageResponse(Call call, Response mainPageResponse, Callback callback) throws IOException {
        assert mainPageResponse.body() != null;
        Document document = Jsoup.parse(mainPageResponse.body().string());
        buttonText = document.select(".btn-session").text();
        todaySessionCompleted = !document.select("#student_panel > h4").isEmpty();
        instalingVersion = document.select("#footer > div > p.span4.text-center").text();
        String homeworkCountText = document.select("#student_panel > div.alert.alert-info > strong").text();
        String[] homeworkCountArray = homeworkCountText.split(":");
        if (homeworkCountArray.length > 1) {
            homeworkCount = Integer.parseInt(homeworkCountArray[1].replace(" ", "").trim());
        }
        callback.onResponse(call, mainPageResponse);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getHomeworkCount() {
        return homeworkCount;
    }

    public boolean isTodaySessionCompleted() {
        return todaySessionCompleted;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getInstalingVersion() {
        return instalingVersion;
    }

    public String getAppID() {
        return appID;
    }

    public String getMessage() {
        return message;
    }

    public String getPhpSessionID() {
        return phpSessionID;
    }

    public String getStudentID() {
        return studentID;
    }
}
