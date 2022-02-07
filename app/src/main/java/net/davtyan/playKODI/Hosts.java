package net.davtyan.playKODI;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hosts extends AppCompatActivity {
    static final String APP_PREFERENCES = "MySettings";

    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //
    private static SharedPreferences mSettings;
    ListView list;
    List<Host> hosts = new ArrayList<>();
    String[] hostNicknameArr;
    String[] hostAddressArr;
    String[] colorCodesArr;
    HostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.AppThemeDark);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) { //checking the theme
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppThemeDark);
            }
        }
        setContentView(R.layout.hosts);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.AppThemeDark);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) { //checking the theme
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppThemeDark);
            }
        }
        setContentView(R.layout.hosts);

        updateAdapter();

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intentHostEditor = new Intent(Hosts.this, HostEditor.class);
            intentHostEditor.putExtra("ID", position);
            startActivity(intentHostEditor);
        });
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            String title = hosts.get(position).nickName;
            if (title.equalsIgnoreCase("")) {
                title = hosts.get(position).host;
            }
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton("Edit", (dialog, whichButton) -> {
                        Intent intentHostEditor = new Intent(Hosts.this, HostEditor.class);
                        intentHostEditor.putExtra("ID", position);
                        startActivity(intentHostEditor);
                    })
                    .setNegativeButton("Delete", (dialog, whichButton) -> {
                        hosts.remove(position);
                        SharedPreferences.Editor editor = mSettings.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(hosts);
                        editor.putString("hosts", json);
                        editor.apply();
                        updateAdapter();
                        adapter.notifyDataSetChanged();
                        list.invalidateViews();
                    })
                    .show();
            return true;
        });
    }

    public void updateAdapter() {
        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        hosts.clear();
        hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));

        List<String> hostNickname = new ArrayList<>();
        List<String> hostAddress = new ArrayList<>();
        List<String> colorCodes = new ArrayList<>();
        for (Host host : hosts) {
            hostNickname.add(host.nickName);
            if (!host.port.equals("")) {
                hostAddress.add(host.host + ":" + host.port);
            } else {
                hostAddress.add(host.host);
            }
            colorCodes.add(host.color);
        }
        hostNicknameArr = new String[hostNickname.size()];
        hostAddressArr = new String[hostAddress.size()];
        colorCodesArr = new String[colorCodes.size()];
        hostNicknameArr = hostNickname.toArray(hostNicknameArr);
        hostAddressArr = hostAddress.toArray(hostAddressArr);
        colorCodesArr = colorCodes.toArray(colorCodesArr);

        adapter = new HostAdapter(this,
                hostNicknameArr,
                hostAddressArr,
                colorCodesArr);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonAddHost) {
            Intent intentHostEditor = new Intent(Hosts.this, HostEditor.class);
            if (hosts == null || hosts.size() == 0) {
                intentHostEditor.putExtra("ID", 0);
            } else {
                intentHostEditor.putExtra("ID", hosts.size());
            }
            startActivity(intentHostEditor);

        } else if (id == R.id.buttonCloseHostList) { // button Send to KODI
            finish();
        }

    }
}