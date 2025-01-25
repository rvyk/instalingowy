package pl.rvyk.instapp.solving;

import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.rvyk.instapp.scrapper.GenerateQuestion.GenerateQuestion;

public class GenerateQuestionRequestHelper {

    public interface GenerateQuestionResponseListener {
        void onSuccess(String question, String usageExample, String questionId);
        void onFinish(boolean ended, String instalingDays, String words, String correct);
        void onError(Throwable error);
    }

    public static void sendGenerateQuestionRequest(Context context, String phpSessionId, String appId, String studentId, final GenerateQuestionResponseListener listener) {
        GenerateQuestion generateQuestion = new GenerateQuestion();
        generateQuestion.generate(phpSessionId, appId, studentId, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (generateQuestion.isSuccess()) {
                    if (generateQuestion.isEnded()) {
                        listener.onFinish(
                            true,
                            String.valueOf(generateQuestion.getInstalingDays()),
                            String.valueOf(generateQuestion.getWords()),
                            String.valueOf(generateQuestion.getCorrect())
                        );
                    } else {
                        listener.onSuccess(
                            generateQuestion.getGeneratedWord(),
                            generateQuestion.getQuestion(),
                            generateQuestion.getQuestionID()
                        );
                    }
                } else {
                    listener.onError(new Throwable("Failed to generate question"));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError(e);
            }
        });
    }
}
