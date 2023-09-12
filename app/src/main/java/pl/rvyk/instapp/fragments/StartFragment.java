package pl.rvyk.instapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pl.rvyk.instapp.LoginActivity;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.SolvingActivity;

public class StartFragment extends Fragment {

    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_start, container, false);

        TextView loginTextView = view.findViewById(R.id.nickname);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        TextView sessionStatusView = view.findViewById(R.id.sessionStatus);
        Button startButton = view.findViewById(R.id.startButton);

        sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);

        String login = sharedPreferences.getString("login", "");
        String appid = sharedPreferences.getString("appid", "");
        String studentid = sharedPreferences.getString("studentid", "");
        String phpsessid = sharedPreferences.getString("phpsessid", "");
        boolean todaySessionCompleted = sharedPreferences.getBoolean("todaySessionCompleted", false);

        loginTextView.setText(login);

        if (todaySessionCompleted) {
            sessionStatusView.setText(R.string.today_session_completed);
        } else {
            sessionStatusView.setText(R.string.today_session_uncompleted);
        }

        startButton.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putString("appid", appid);
            bundle.putString("studentid", studentid);
            bundle.putString("phpsessid", phpsessid);

            Intent intent = new Intent(getContext(), SolvingActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });
        logoutButton.setOnClickListener(view12 -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();

        });
        return view;
    }
}
