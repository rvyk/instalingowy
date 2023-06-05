package pl.rvyk.instapp.solving;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SendAnswerRequestHelper {

    public interface SendAnswerResponseListener {
        void onSuccess(Integer grade, String word, String usageExample);
        void onError(Throwable error);
    }

    public static void sendAnswerRequest(Context context, String phpSessionId, String appId, String studentId, String questionId, String answer, String login, final SendAnswerResponseListener listener) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("phpsessid", phpSessionId);
            jsonParams.put("appid", appId);
            jsonParams.put("studentid", studentId);
            jsonParams.put("questionid", questionId);
            jsonParams.put("answer", answer);
            jsonParams.put("login", login);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://api.ezinstaling.lol/api/v1/instaling/sendanswer", jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject questionObject = response.optJSONObject("output");
                        if (questionObject != null) {
                            Integer grade = questionObject.optInt("grade");
                            String word = questionObject.optString("word");
                            String usageExample = questionObject.optString("usage_example");
                            listener.onSuccess(grade, word, usageExample);
                        } else {
                            Throwable throwable = new Throwable("Output object is null");
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
