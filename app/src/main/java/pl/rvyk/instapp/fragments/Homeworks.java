package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.rvyk.instapp.R;

public class Homeworks extends Fragment {

    private ViewGroup container;
    private LinearLayout progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private HomeworkAdapter homeworkAdapter;
    private SharedPreferences sharedPreferences;
    private List<Homework> homeworkList;
    private View notFoundView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homework, container, false);
        this.container = container;
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        homeworkList = new ArrayList<>();
        homeworkAdapter = new HomeworkAdapter(homeworkList, requireContext());
        recyclerView.setAdapter(homeworkAdapter);
        notFoundView = inflater.inflate(R.layout.fragment_homeworks_notfound, container, false);
        notFoundView.setVisibility(View.GONE);
        container.addView(notFoundView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadHomeworks();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.VISIBLE);
        loadHomeworks();
    }

    private void loadHomeworks() {
        JSONObject requestObject = new JSONObject();
        try {
            sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);
            String phpsessid = sharedPreferences.getString("phpsessid", "");
            requestObject.put("phpsessid", phpsessid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://api.ezinstaling.lol/api/v1/instaling/gethomeworks";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray homeworksTodo = response.getJSONArray("homeworksTodo");
                                JSONArray homeworksDone = response.getJSONArray("homeworksDone");

                                homeworkList.clear();

                                for (int i = 0; i < homeworksTodo.length(); i++) {
                                    JSONObject homework = homeworksTodo.getJSONObject(i);
                                    String title = homework.getString("title");
                                    String deadline = homework.getString("deadline");
                                    String homeworkLink = homework.getString("homeworkLink");
                                    homeworkList.add(new Homework(title, deadline, homeworkLink, null, false));
                                }

                                for (int i = 0; i < homeworksDone.length(); i++) {
                                    JSONObject homework = homeworksDone.getJSONObject(i);
                                    String title = homework.getString("title");
                                    String deadline = homework.getString("deadline");
                                    String homeworkLink = homework.getString("homeworkLink");
                                    String grade = homework.getString("grade");
                                    homeworkList.add(new Homework(title, deadline, homeworkLink, grade, true));
                                }

                                homeworkAdapter.notifyDataSetChanged();

                                if (homeworksTodo.length() == 0 && homeworksDone.length() == 0) {
                                    notFoundView.setVisibility(View.VISIBLE);
                                } else {
                                    notFoundView.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(jsonObjectRequest);
    }

    public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {
        private List<Homework> homeworkList;
        private Context context;

        public HomeworkAdapter(List<Homework> homeworkList, Context context) {
            this.homeworkList = homeworkList;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_homework, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Homework homework = homeworkList.get(position);

            if (homework.isDone()) {
                holder.statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDone));
            } else {
                holder.statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorNotDone));
            }

            holder.titleTextView.setText(getString(R.string.homework_exercise) + " " + homework.getTitle());
            holder.deadlineTextView.setText(getString(R.string.homework_term) + " " + homework.getDeadline());
            if (homework.getGrade() != null) {
                holder.extraInfoTextView.setText(getString(R.string.homework_grade) + " " + homework.getGrade());
            } else {
                holder.extraInfoTextView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return homeworkList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View statusBar;
            public TextView titleTextView, deadlineTextView, extraInfoTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                statusBar = itemView.findViewById(R.id.statusBar);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                deadlineTextView = itemView.findViewById(R.id.deadlineTextView);
                extraInfoTextView = itemView.findViewById(R.id.extraInfoTextView);
            }
        }
    }
}
