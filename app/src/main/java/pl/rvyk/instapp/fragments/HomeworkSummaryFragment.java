package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.scrapper.Homeworks.GetHomeworkDetails;
import pl.rvyk.instapp.scrapper.Homeworks.SaveHomework;
import pl.rvyk.instapp.utils.SnackbarController;

public class HomeworkSummaryFragment extends Fragment {

    private TextView title, term, exercise;
    private TextInputLayout answerLayout, teacherNoteLayout, gradeLayout;
    private TextInputEditText answer, teacherNote, grade;
    private SharedPreferences sharedPreferences;
    private MaterialButton saveButton, sendButton, additionalMaterialButton;
    private MaterialCardView information, note, gradeView, additionalMaterials;
    private LinearLayout loaderLayout, content;

    private String additionalMaterialLink;
    private String homeworkLink;

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

        additionalMaterials = view.findViewById(R.id.externalLinkCard);
        additionalMaterialButton = view.findViewById(R.id.externalLink);

        Bundle bundle = getArguments();
        if (bundle != null) {
            homeworkLink = bundle.getString("link");
            getHomeworks(homeworkLink, phpsessid);
        }

        toolbar.setNavigationOnClickListener(view1 -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });

        saveButton.setOnClickListener(view12 -> {
            String userResponse = answer.getText().toString();
            sendHomeworkRequest(phpsessid, userResponse, "save");
        });

        sendButton.setOnClickListener(view13 -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.homework_dialog_title));
            builder.setMessage(getString(R.string.homework_dialog_message));
            builder.setNegativeButton(getActivity().getResources().getString(R.string.reportDialogCancel), null);
            builder.setPositiveButton(getActivity().getResources().getString(R.string.homework_dialog_confirm), (dialog, which) -> {
                String userResponse = answer.getText().toString();
                sendHomeworkRequest(phpsessid, userResponse, "save_and_send");
            });
            builder.show();
        });

        additionalMaterialButton.setOnClickListener(view14 -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(additionalMaterialLink));
                startActivity(intent);
            } catch (Exception e) {
                SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
            }
        });
        return view;
    }

    private void sendHomeworkRequest(String phpsessid, String userResponse, String action) {
        SaveHomework saveHomework = new SaveHomework();
        try {
            saveHomework.save(phpsessid, homeworkLink, userResponse, action, new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!isAdded()) {
                        return;
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (saveHomework.isSuccess()) {
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
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!isAdded()) {
                        return;
                    }

                    requireActivity().runOnUiThread(() -> 
                        SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true)
                    );
                }
            });
        } catch (IOException e) {
            requireActivity().runOnUiThread(() -> 
                SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true)
            );
        }
    }

    private void getHomeworks(String link, String phpsessid) {
        showLoader();

        GetHomeworkDetails getHomeworkDetails = new GetHomeworkDetails();
        getHomeworkDetails.details(phpsessid, link, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (getHomeworkDetails.isSuccess()) {
                        if (getHomeworkDetails.isDone()) {
                            answer.setEnabled(false);
                            sendButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);
                            information.setVisibility(View.GONE);

                            String homeworkGrade = getHomeworkDetails.getGrade();
                            if (homeworkGrade != null && !homeworkGrade.isEmpty()) {
                                if (homeworkGrade.equals("0")) {
                                    grade.setText(R.string.homework_no_grade);
                                } else {
                                    grade.setText(homeworkGrade);
                                }
                                gradeView.setVisibility(View.VISIBLE);
                            }
                        }

                        String note = getHomeworkDetails.getNote();
                        if (note != null && !note.isEmpty()) {
                            teacherNote.setText(note);
                            HomeworkSummaryFragment.this.note.setVisibility(View.VISIBLE);
                        }

                        String ytLink = getHomeworkDetails.getYtLink();
                        if (ytLink != null && !ytLink.isEmpty()) {
                            additionalMaterialLink = ytLink;
                            additionalMaterials.setVisibility(View.VISIBLE);
                        }

                        title.setText(getHomeworkDetails.getTitle());
                        term.setText(getHomeworkDetails.getDeadline());
                        exercise.setText(getHomeworkDetails.getExercise());
                        answer.setText(getHomeworkDetails.getAnswer());
                    } else {
                        SnackbarController.showSnackbar(getActivity(), content, new Throwable("Failed to get homework details"), getResources().getString(R.string.unkown_error), true);
                    }
                    hideLoader();
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    SnackbarController.showSnackbar(getActivity(), content, e, getResources().getString(R.string.unkown_error), true);
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
        content.setVisibility(View.GONE);
    }

    private void hideLoader() {
        if (!isAdded()) {
            return;
        }

        loaderLayout.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }
}
