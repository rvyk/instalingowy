package pl.rvyk.instapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.scrapper.Homeworks.GetHomeworks;
import pl.rvyk.instapp.scrapper.Homeworks.HomeworkItem;
import pl.rvyk.instapp.utils.SnackbarController;

public class HomeworkFragment extends Fragment {

    private static final String TAG = "HomeworkFragment";

    private RecyclerView recyclerView;
    private LinearLayout notFoundLayout;
    private LinearLayout loaderLayout, content;
    private SharedPreferences sharedPreferences;

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

        @SuppressLint("RestrictedApi") ActionMenuItemView refresh = view.findViewById(R.id.refreshButton);

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

        sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);
        String phpSessionId = sharedPreferences.getString("phpsessid", "");
        String appId = sharedPreferences.getString("appid", "");
        String studentId = sharedPreferences.getString("studentid", "");

        GetHomeworks getHomeworks = new GetHomeworks();
        getHomeworks.getHomeworks(phpSessionId, appId, studentId, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    GetHomeworks.HomeworkResult result = getHomeworks.getResult();
                    if (result != null && result.isSuccess()) {
                        if (!result.getHomeworksTodo().isEmpty() || !result.getHomeworksDone().isEmpty()) {
                            showHomeworks(result.getHomeworksTodo(), result.getHomeworksDone());
                        } else {
                            showNotFoundLayout();
                        }
                    } else {
                        showNotFoundLayout();
                    }
                    hideLoader();
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
                    showNotFoundLayout();
                    hideLoader();
                });
            }
        });
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

    private void showHomeworks(java.util.List<HomeworkItem> homeworksTodo, java.util.List<HomeworkItem> homeworksDone) {
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

        private final java.util.List<HomeworkItem> homeworksTodo;
        private final java.util.List<HomeworkItem> homeworksDone;

        HomeworkAdapter(java.util.List<HomeworkItem> homeworksTodo, java.util.List<HomeworkItem> homeworksDone) {
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
            HomeworkItem homework;

            if (position < homeworksTodo.size()) {
                homework = homeworksTodo.get(position);
                holder.statusBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorNotDone));
            } else {
                int donePosition = position - homeworksTodo.size();
                homework = homeworksDone.get(donePosition);
                holder.statusBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorDone));
            }

            holder.titleTextView.setText(homework.getTitle());
            holder.deadlineTextView.setText(homework.getDeadline());

            String grade = homework.getGrade();
            if (grade != null && !grade.isEmpty()) {
                if (grade.equals("0")) {
                    holder.extraInfoTextView.setText(holder.itemView.getContext().getString(R.string.homework_no_grade));
                } else {
                    holder.extraInfoTextView.setText(grade);
                }
                holder.extraInfoTextView.setVisibility(View.VISIBLE);
            } else {
                holder.extraInfoTextView.setVisibility(View.GONE);
            }

            holder.cardView.setOnClickListener(view -> {
                if (!isAdded()) {
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("link", homework.getHomeworkLink());

                HomeworkSummaryFragment homeworkSummaryFragment = new HomeworkSummaryFragment();
                homeworkSummaryFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, homeworkSummaryFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return homeworksTodo.size() + homeworksDone.size();
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
