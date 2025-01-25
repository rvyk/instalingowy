package pl.rvyk.instapp.solving;

import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.rvyk.instapp.scrapper.SendAnswer.SendAnswer;

public class SendAnswerRequestHelper {

    public interface SendAnswerResponseListener {
        void onSuccess(Integer grade, String word, String usageExample);
        void onError(Throwable error);
    }

    public static void sendAnswerRequest(Context context, String phpSessionId, String appId, String studentId, String questionId, String answer, String login, final SendAnswerResponseListener listener) {
        SendAnswer sendAnswer = new SendAnswer();
        sendAnswer.send(phpSessionId, appId, studentId, questionId, answer, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (sendAnswer.isSuccess()) {
                    listener.onSuccess(
                        sendAnswer.getGrade().intValue(),
                        sendAnswer.getWord(),
                        sendAnswer.getQuestion()
                    );
                } else {
                    listener.onError(new Throwable("Failed to send answer"));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError(e);
            }
        });
    }
}
