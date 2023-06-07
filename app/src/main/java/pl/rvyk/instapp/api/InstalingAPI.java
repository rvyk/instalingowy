package pl.rvyk.instapp.api;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstalingAPI {

    private static final String mozillaUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";

    public static void loginToInstaling(String email, String password, final InstalingLoginCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("action", "login")
                .add("log_email", email)
                .add("log_password", password)
                .build();

        Request request = new Request.Builder()
                .url("https://instaling.pl/teacher.php?page=teacherActions")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", mozillaUserAgent)
                .post(formBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onLoginError("Caught error in OKHTTP: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("response1", String.valueOf(response.headers()));
                Log.d("call", call.toString());
                if (response.isSuccessful() && response.header("location") != null && response.header("location").startsWith("learning/dispatcher")) {
                    final String phpsessid = response.header("set-cookie").split(";")[0];
                    final String locationHeader = response.header("location");
                    String studentid = "";
                    if (locationHeader != null) {
                        String[] parts = locationHeader.split("\\?");
                        if (parts.length > 1) {
                            studentid = parts[1];
                        }
                    }

                    final String finalStudentid = studentid;

                    Request dispatcherRequest = new Request.Builder()
                            .url("https://instaling.pl/learning/dispatcher.php")
                            .header("User-Agent", mozillaUserAgent)
                            .header("Cookie", phpsessid)
                            .build();

                    client.newCall(dispatcherRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callback.onLoginError("Caught error in OKHTTP: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d("response2", String.valueOf(response.headers()));
                            String appid = "app=app_84";
                            if (response.header("set-cookie") != null) {
                                appid = response.header("set-cookie").split(";")[0];
                            }
                            final String finalAppid = appid;

                            Request mainRequest = new Request.Builder()
                                    .url("https://instaling.pl/student/pages/mainPage.php?" + finalStudentid)
                                    .header("User-Agent", mozillaUserAgent)
                                    .header("Cookie", phpsessid + "; ")
                                    .build();

                            client.newCall(mainRequest).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    callback.onLoginError("Caught error in OKHTTP: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String responseBody = response.body().string();
                                        Document document = Jsoup.parse(responseBody);

                                        String buttonText = document.select(".sesion").first().text();
                                        boolean todaySessionCompletedBool = document.select("#student_panel > h4").first() != null;

                                        callback.onLoginResult(true, "Login Successfully", phpsessid, finalAppid, finalStudentid, buttonText, todaySessionCompletedBool);
                                    } else {
                                        callback.onLoginError("Main response is not successful or body is null");
                                    }
                                }
                            });
                        }
                    });
                } else {
                    callback.onLoginResult(false, "Login Failed", null, null, null, null, false);
                }
            }
        });
    }

    public interface InstalingLoginCallback {
        void onLoginResult(boolean success, String message, String phpsessid, String appid, String studentid, String buttonText, boolean todaySessionCompleted);
        void onLoginError(String error);
    }
}
