package pl.rvyk.instapp.api;

import androidx.annotation.NonNull;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import java.io.IOException;

public class InstalingLogin {

    public static void loginToInstaling(String email, String password) {
        String mozillaUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36";

        RequestBody formBody = new FormEncodingBuilder()
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
            public void onFailure(Request request, IOException e) {
                System.out.println(request);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Headers headers = response.headers();
                    System.out.println(headers);
                    String phpsessid = headers.values("Set-Cookie").get(0).split(";")[0];
                    System.out.println(phpsessid);
                }
            }
        });
    }
}
