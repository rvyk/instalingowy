package pl.rvyk.instapp.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VersionFetcher {
    public interface FetchAPIVersionListener {
        void onSuccess(String version);
        void onError(Throwable error);
    }

    public static void fetchAPIVersion(Context context, final FetchAPIVersionListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = "https://api.ezinstaling.lol/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String version = response.getString("version");
                            listener.onSuccess(version);
                        } catch (JSONException e) {
                            listener.onError(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
