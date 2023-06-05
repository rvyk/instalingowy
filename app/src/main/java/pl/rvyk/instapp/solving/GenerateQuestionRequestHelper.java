package pl.rvyk.instapp.solving;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class GenerateQuestionRequestHelper {

    public interface GenerateQuestionResponseListener {
        void onSuccess(String question, String usageExample, String questionId);
        void onFinish(boolean ended);
        void onError(Throwable error);
    }

    public static void sendGenerateQuestionRequest(Context context, String phpSessionId, String appId, String studentId, final GenerateQuestionResponseListener listener) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("phpsessid", phpSessionId);
            jsonParams.put("appid", appId);
            jsonParams.put("studentid", studentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://api.ezinstaling.lol/api/v1/instaling/generatequestion", jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject questionObject = response.optJSONObject("question");
                        boolean ended = response.optBoolean("ended");
                        if (questionObject != null) {
                            String question = questionObject.optString("translations");
                            String questionId = questionObject.optString("id");
                            String usageExample = questionObject.optString("usage_example");
                            listener.onSuccess(question, usageExample, questionId);
                        } else if (ended == true) {
                            listener.onFinish(ended);
                        } else {
                            Throwable throwable = new Throwable("Question object is null");
                            listener.onError(throwable);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Throwable throwable = new Throwable("VolleyError: " + error.getMessage());
                        listener.onError(throwable);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
