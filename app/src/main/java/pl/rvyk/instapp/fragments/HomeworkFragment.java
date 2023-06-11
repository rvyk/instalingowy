package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.rvyk.instapp.R;
import pl.rvyk.instapp.utils.SnackbarController;

public class HomeworkFragment extends Fragment {

    private static final String TAG = "HomeworkFragment";
    private static final String API_URL = "https://api.ezinstaling.lol/api/v1/instaling/gethomeworks";

    private RecyclerView recyclerView;
    private LinearLayout notFoundLayout;
    private LinearLayout loaderLayout, content;
    private SharedPreferences sharedPreferences;

    private RequestQueue requestQueue;

    private boolean isButtonClickable = true;

    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homework, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        notFoundLayout = view.findViewById(R.id.homeworks_notfound);
        loaderLayout = view.findViewById(R.id.progressBar);
        content = view.findViewById(R.id.fragment_homework);

        ActionMenuItemView refresh = view.findViewById(R.id.refreshButton);

        requestQueue = Volley.newRequestQueue(requireContext());

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isButtonClickable) {
                    isButtonClickable = false;
                    refresh.setEnabled(false);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isButtonClickable = true;
                            refresh.setEnabled(true);
                        }
                    }, 1500);

                    getHomeworks();
                }
            }
        });

        getHomeworks();

        return view;
    }

    private void getHomeworks() {
        showLoader();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }

                        Log.d(TAG, "Response: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                JSONArray homeworksTodo = jsonObject.getJSONArray("homeworksTodo");
                                JSONArray homeworksDone = jsonObject.getJSONArray("homeworksDone");

                                if (homeworksTodo.length() > 0 || homeworksDone.length() > 0) {
                                    showHomeworks(homeworksTodo, homeworksDone);
                                } else {
                                    showNotFoundLayout();
                                }
                            } else {
                                showNotFoundLayout();
                            }
                        } catch (JSONException e) {
                            SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
                            showNotFoundLayout();
                        }

                        hideLoader();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }

                        SnackbarController.showSnackbar(getActivity(), content, error, getResources().getString(R.string.unkown_error), true);
                        showNotFoundLayout();
                        hideLoader();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);
                String phpsessid = sharedPreferences.getString("phpsessid", "");

                Map<String, String> params = new HashMap<>();
                params.put("phpsessid", phpsessid);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void showLoader() {
        if (!isAdded()) {
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        notFoundLayout.setVisibility(View.GONE);
    }

    private void hideLoader() {
        if (!isAdded()) {
            return;
        }

        loaderLayout.setVisibility(View.GONE);
    }

    private void showNotFoundLayout() {
        if (!isAdded()) {
            return;
        }

        notFoundLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showHomeworks(JSONArray homeworksTodo, JSONArray homeworksDone) {
        if (!isAdded()) {
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        notFoundLayout.setVisibility(View.GONE);

        HomeworkAdapter adapter = new HomeworkAdapter(homeworksTodo, homeworksDone);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {

        private JSONArray homeworksTodo;
        private JSONArray homeworksDone;

        HomeworkAdapter(JSONArray homeworksTodo, JSONArray homeworksDone) {
            this.homeworksTodo = homeworksTodo;
            this.homeworksDone = homeworksDone;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_homework, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject homework;

                if (position < homeworksTodo.length()) {
                    homework = homeworksTodo.getJSONObject(position);
                    holder.statusBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNotDone));
                } else {
                    int donePosition = position - homeworksTodo.length();
                    homework = homeworksDone.getJSONObject(donePosition);
                    holder.statusBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorDone));
                }

                final String link = homework.getString("homeworkLink"); // Pobierz link do zadania

                String title = homework.getString("title");
                String deadline = homework.getString("deadline");
                String extraInfo = "";

                if (homework.has("grade")) {
                    String grade = homework.getString("grade");

                    if (grade.equals("0")) {
                        extraInfo = holder.itemView.getContext().getString(R.string.homework_no_grade);
                    } else {
                        extraInfo = grade;
                    }
                } else {
                    holder.extraInfoTextView.setVisibility(View.GONE);
                }

                holder.titleTextView.setText(holder.itemView.getContext().getString(R.string.homework_exercise) + " " + title);
                holder.deadlineTextView.setText(holder.itemView.getContext().getString(R.string.homework_term) + " " + deadline);
                holder.extraInfoTextView.setText(holder.itemView.getContext().getString(R.string.homework_grade) + " " + extraInfo);

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(view.getContext(), link, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (JSONException e) {
                if (!isAdded()) {
                    return;
                }

                SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
            }
        }

        @Override
        public int getItemCount() {
            return homeworksTodo.length() + homeworksDone.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            View statusBar;
            MaterialTextView titleTextView;
            MaterialTextView deadlineTextView;
            MaterialTextView extraInfoTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardView);
                statusBar = itemView.findViewById(R.id.statusBar);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                deadlineTextView = itemView.findViewById(R.id.deadlineTextView);
                extraInfoTextView = itemView.findViewById(R.id.extraInfoTextView);
            }
        }
    }
}
