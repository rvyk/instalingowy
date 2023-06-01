package pl.rvyk.instapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.rvyk.instapp.fragments.Accounts;
import pl.rvyk.instapp.fragments.Settings;
import pl.rvyk.instapp.fragments.Start;

public class PageAdapter extends FragmentStateAdapter {

    private String login;
    private String password;
    private String appid;
    private String childid;
    private String phpsessid;

    public PageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
               return new Start();

            case 1:
                return new Accounts();

            case 2:
                return new Settings();

            default:
                return new Start();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

