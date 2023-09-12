package pl.rvyk.instapp.manager;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private List<Account> accounts;
    private static final String SHARED_PREF_NAME = "AccountManager";
    private static final String KEY_ACCOUNTS = "accounts";

    public AccountManager() {
        accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void saveAccounts(Context context) {
        JSONArray jsonArray = new JSONArray();

        for (Account account : accounts) {
            try {
                JSONObject jsonAccount = new JSONObject();
                jsonAccount.put("login", account.getLogin());
                jsonAccount.put("password", account.getPassword());
                jsonArray.put(jsonAccount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCOUNTS, jsonArray.toString());
        editor.apply();
    }

    public void loadAccounts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String jsonAccounts = sharedPreferences.getString(KEY_ACCOUNTS, null);

        accounts.clear();

        if (jsonAccounts != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonAccounts);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonAccount = jsonArray.getJSONObject(i);
                    String login = jsonAccount.getString("login");
                    String password = jsonAccount.getString("password");
                    Account account = new Account(login, password);
                    accounts.add(account);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

