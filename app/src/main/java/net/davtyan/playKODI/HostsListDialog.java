package net.davtyan.playKODI;


import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class HostsListDialog extends AppCompatActivity implements AsyncResponse {
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

        setTheme(R.style.Theme_Transparent);

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) { //checking the theme
                setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
            }
        }

        setContentView(R.layout.hosts_view_dialog);

        updateAdapter();

        if (hosts.size() == 1) {
            Intent intent = getIntent();
            intent.putExtra("position", 0);
            playLink(intent);
            finish();
        }

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = getIntent();
            intent.putExtra("position", position);
            playLink(intent);
            finish();
        });
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            String title = hosts.get(position).nickName;
            if (title.equalsIgnoreCase("")) {
                title = hosts.get(position).host;
            }
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setPositiveButton(R.string.play_once, (dialog, whichButton) -> {
                        Intent intent = getIntent();
                        intent.putExtra("position", position);
                        playLink(intent);
                        finish();
                    })
                    .setNegativeButton(R.string.make_default_and_play, (dialog, whichButton) -> {
                        SharedPreferences.Editor editor = mSettings.edit();
                        String hostFullAddress = hosts.get(position).host + ":" + hosts.get(position).port;
                        editor.putBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, true);
                        editor.putString(APP_PREFERENCES_DEFAULT_HOST, hostFullAddress);
                        editor.apply();

                        Intent intent = getIntent();
                        intent.putExtra("position", position);
                        playLink(intent);
                        finish();
                    })
                    .show();

            return true;
        });
    }

    public void playLink(Intent intent) {
        int position = intent.getExtras().getInt("position");
        String textToPaste = intent.getExtras().getString("link");
        String[] requestParams = new String[10];

        requestParams[0] = hosts.get(position).host;
        requestParams[1] = hosts.get(position).port;
        requestParams[2] = hosts.get(position).login;
        requestParams[3] = hosts.get(position).password;
        requestParams[4] = textToPaste;

        String event = intent.getExtras().getString("event");
        if (event.equalsIgnoreCase("YOUTUBE")) {
            requestParams[5] = "OPEN";
        } else if (event.equalsIgnoreCase("YOUTUBEADD")) {
            requestParams[5] = "ADD";
        } else {
            requestParams[5] = event;
            //coping to clipboard
            if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false)) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", textToPaste);
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            }

            String fullName = hosts.get(position).host + ":" + hosts.get(position).port;
            if (!hosts.get(position).nickName.equals("")) fullName = hosts.get(position).nickName;
            String toastMessage = getResources().getString(R.string.messageSendingLink) + " " + fullName;
            //preview the link
            if (mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false)) {
                toastMessage = toastMessage + "\n" + textToPaste;
            }
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }

        //send request to play
        MakeRequest myMakeRequest = new MakeRequest();
        myMakeRequest.execute(requestParams);
        myMakeRequest.delegate = this;
    }

    public void updateAdapter() {
        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        hosts.clear();
        if (!json.equalsIgnoreCase("")) {
            hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));
        }
        if (hosts.size() == 0) {
            finish();
        }

        List<String> hostNickname = new ArrayList<>();
        List<String> hostAddress = new ArrayList<>();
        List<String> colorCodes = new ArrayList<>();
        for (Host host : hosts) {
            hostNickname.add(host.nickName);
            hostAddress.add(host.host + ":" + host.port);
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
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonAddHost) {
            Intent intentHostEditor = new Intent(HostsListDialog.this, HostEditor.class);
            if (hosts == null || hosts.size() == 0) {
                intentHostEditor.putExtra("ID", 0);
            } else {
                intentHostEditor.putExtra("ID", hosts.size());
            }
            startActivity(intentHostEditor);

        } else if (id == R.id.buttonCloseHostList) {
            if (hosts.size() > 0) {
                Intent intentMyActivity = new Intent(HostsListDialog.this, MyActivity.class);
                startActivity(intentMyActivity);
            }
            finish();
        }

    }

    public void onBackPressed() { // Back button
        if (hosts.size() > 0) {
            Intent intentMyActivity = new Intent(HostsListDialog.this, MyActivity.class);
            startActivity(intentMyActivity);
        }
        finish();
    }

    @Override
    public void processFinish(String output) {
        //toast messages about the result

        switch (output) {
            case "Illegal Argument ERROR":
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageLinkHaveWhitespace), Toast.LENGTH_SHORT).show();
                break;
            case "Network ERROR":
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageNetworkError), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageLinkSuccess), Toast.LENGTH_SHORT).show();
                break;
        }
    }

}

