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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import pl.rvyk.instapp.R;
import pl.rvyk.instapp.utils.SnackbarController;

public class HomeworkSummaryFragment extends Fragment {

    private static final String API_URL = "https://api.ezinstaling.lol/api/v1/instaling/gethomeworksummary";
    private static final String API_URL_SAVE_HOMEWORK = "https://api.ezinstaling.lol/api/v1/instaling/savehomework";

    private TextView title, term, exercise;
    private TextInputLayout answerLayout, teacherNoteLayout, gradeLayout;
    private TextInputEditText answer, teacherNote, grade;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private MaterialButton saveButton, sendButton;
    private MaterialCardView information, note, gradeView;
    private LinearLayout loaderLayout, content;

    private String homeworkLink, homeworkExercise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homework_summary, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);
        String phpsessid = sharedPreferences.getString("phpsessid", "");

        MaterialToolbar toolbar = view.findViewById(R.id.materialToolbar);

        title = view.findViewById(R.id.titleTextView);
        term = view.findViewById(R.id.deadlineTextView);
        answerLayout = view.findViewById(R.id.homeworkAnswer);
        answer = answerLayout.findViewById(R.id.homeworkAnswerInput);
        exercise = view.findViewById(R.id.exercise);
        saveButton = view.findViewById(R.id.saveButton);
        sendButton = view.findViewById(R.id.sendButton);
        information = view.findViewById(R.id.information);
        note = view.findViewById(R.id.teacherNote);
        teacherNoteLayout = view.findViewById(R.id.homeworkTeacherNote);
        teacherNote = teacherNoteLayout.findViewById(R.id.homeworkTeacherNoteText);
        gradeLayout = view.findViewById(R.id.homeworkGradeLayout);
        gradeView = view.findViewById(R.id.homeworkGradeCard);
        grade = gradeLayout.findViewById(R.id.homeworkGrade);
        loaderLayout = view.findViewById(R.id.progressBar);
        content = view.findViewById(R.id.fragmentHomeworkSummary);

        requestQueue = Volley.newRequestQueue(requireContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            homeworkLink = bundle.getString("homeworkLink");
            homeworkExercise = bundle.getString("homeworkExercise");
            getHomeworks(homeworkLink, phpsessid);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userResponse = answer.getText().toString();
                sendHomeworkRequest(phpsessid, userResponse, "save");
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(getString(R.string.homework_dialog_title));
                builder.setMessage(getString(R.string.homework_dialog_message));
                builder.setNegativeButton(getActivity().getResources().getString(R.string.reportDialogCancel), null);
                builder.setPositiveButton("Potwierdzam", (dialog, which) -> {
                    String userResponse = answer.getText().toString();
                    sendHomeworkRequest(phpsessid, userResponse, "save_and_send");
                });
                builder.show();
            }
        });

        return view;
    }

    private void sendHomeworkRequest(String phpsessid, String userResponse, String action) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("phpsessid", phpsessid);
            jsonParams.put("link", homeworkLink);
            jsonParams.put("exercise", homeworkExercise);
            jsonParams.put("response", userResponse);
            jsonParams.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL_SAVE_HOMEWORK, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("test", String.valueOf(response));
                        if (!isAdded()) {
                            return;
                        }

                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                if (action.equals("save")) {
                                    Toast.makeText(getActivity(), getString(R.string.homework_saved), Toast.LENGTH_SHORT).show();
                                } else if (action.equals("save_and_send")) {
                                    Toast.makeText(getActivity(), getString(R.string.homework_completed), Toast.LENGTH_SHORT).show();
                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                    fragmentManager.popBackStack();
                                }
                            } else {
                                Throwable error = new Throwable(getString(R.string.unkown_error));
                                SnackbarController.showSnackbar(getActivity(), content, error, getResources().getString(R.string.unkown_error), true);
                                hideLoader();
                            }
                        } catch (JSONException e) {
                            SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
                            hideLoader();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }
                        SnackbarController.showSnackbar(getActivity(), content, error, getResources().getString(R.string.unkown_error), true);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void getHomeworks(String link, String phpsessid) {
        showLoader();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("phpsessid", phpsessid);
            jsonParams.put("link", link);
        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) {
                            return;
                        }

                        try {
                            boolean success = response.getBoolean("success");
                            JSONObject details = response.optJSONObject("details");
                            if (success) {
                                if (response.getBoolean("done")) {
                                    answer.setEnabled(false);
                                    sendButton.setVisibility(View.GONE);
                                    saveButton.setVisibility(View.GONE);
                                    information.setVisibility(View.GONE);
                                    if (details.has("grade")) {
                                        if (details.getString("grade").equals("0")) {
                                            grade.setText(R.string.homework_no_grade);
                                        } else {
                                            grade.setText(details.getString("grade"));
                                        }
                                        gradeView.setVisibility(View.VISIBLE);
                                    }
                                }

                                if (details.has("note")) {
                                    teacherNote.setText(details.getString("note"));
                                    note.setVisibility(View.VISIBLE);
                                }

                                title.setText(response.getString("title"));
                                term.setText(response.getString("deadline"));
                                answer.setText(details.getString("answer"));
                                exercise.setText(details.getString("exercise"));

                                hideLoader();
                            } else {
                                Throwable error = new Throwable(getString(R.string.unkown_error));
                                SnackbarController.showSnackbar(getActivity(), content, error, getResources().getString(R.string.unkown_error), true);
                                hideLoader();
                            }
                        } catch (JSONException e) {
                            SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
                            hideLoader();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            return;
                        }
                        SnackbarController.showSnackbar(getActivity(), content, error, getResources().getString(R.string.unkown_error), true);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void showLoader() {
        if (!isAdded()) {
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        if (!isAdded()) {
            return;
        }

        loaderLayout.setVisibility(View.GONE);
    }
}
