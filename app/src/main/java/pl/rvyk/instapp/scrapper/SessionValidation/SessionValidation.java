package pl.rvyk.instapp.scrapper.SessionValidation;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import pl.rvyk.instapp.scrapper.Scrapper;

import java.io.IOException;
import java.util.Objects;

public class SessionValidation {
    private boolean success;
    private String message;
    private String phpSessionID;

    public void validateSesion(String phpsessid, Callback callback) {
        Request validateSessionRequest = new Request.Builder()
                .url("https://instaling.pl/learning/dispatcher.php")
                .addHeader("User-Agent", Scrapper.mozillaUserAgent)
                .addHeader("Cookie", phpsessid)
                .build();
        Scrapper.client.newCall(validateSessionRequest).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response validateResponse) throws IOException {
                if (Objects.requireNonNull(validateResponse.header("Location")).contains("logout.php")) {
                    success = false;
                    message = "Validation Failed";
                    phpSessionID = phpsessid;
                    callback.onResponse(call, validateResponse);
                    return;
                }
                success = true;
                message = "Validation Successfully";
                phpSessionID = phpsessid;
                callback.onResponse(call, validateResponse);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, new IOException("[SessionValidation] -> Request failed"));
            }
        });
    }

    public String getPhpSessionID() {
        return phpSessionID;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
