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

import pl.rvyk.instapp.MainActivity;
import pl.rvyk.instapp.R;
import pl.rvyk.instapp.SolvingActivity;

public class Start extends Fragment {

    private TextView loginTextView, passwordTextView, appidTextView, childidTextView, phpsessidTextView, buttonTextView;
    private Button destroyAcc, startButton;
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_start, container, false);

        loginTextView = view.findViewById(R.id.loginTextView);
        passwordTextView = view.findViewById(R.id.passwordTextView);
        appidTextView = view.findViewById(R.id.appidTextView);
        childidTextView = view.findViewById(R.id.childidTextView);
        phpsessidTextView = view.findViewById(R.id.phpsessidTextView);
        destroyAcc = view.findViewById(R.id.destroyAcc);
        buttonTextView = view.findViewById(R.id.sessionStatus);
        startButton = view.findViewById(R.id.startButton);

        sharedPreferences = requireActivity().getSharedPreferences("Account1", Context.MODE_PRIVATE);

        String login = sharedPreferences.getString("login", "");
        String password = sharedPreferences.getString("password", "");
        String appid = sharedPreferences.getString("appid", "");
        String studentid = sharedPreferences.getString("studentid", "");
        String phpsessid = sharedPreferences.getString("phpsessid", "");
        String buttonText = sharedPreferences.getString("buttontext", "");

        loginTextView.setText(login);
        passwordTextView.setText(password);
        appidTextView.setText(appid);
        childidTextView.setText(studentid);
        phpsessidTextView.setText(phpsessid);
        buttonTextView.setText(buttonText);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("appid", appid);
                bundle.putString("studentid", studentid);
                bundle.putString("phpsessid", phpsessid);

                Intent intent = new Intent(getContext(), SolvingActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
        destroyAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();

            }
        });
        return view;
    }
}
