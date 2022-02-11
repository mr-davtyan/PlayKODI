package net.davtyan.playKODI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Settings extends AppCompatActivity {

    static final String APP_PREFERENCES = "MySettings";
    static final String APP_PREFERENCES_THEME_DARK = "Theme";
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme";
    static final String APP_PREFERENCES_COPY_LINKS = "CopyLinks";
    static final String APP_PREFERENCES_PREVIEW_LINKS = "Show Link after sending";
    static final String APP_PREFERENCES_USE_DEFAULT_HOST = "UseDefaultHostForShare";
    static final String APP_PREFERENCES_DEFAULT_HOST = "DefaultHostForShare";

    private static SharedPreferences mSettings;
    List<String> hostFullAddress = new ArrayList<>();
    private CheckBox checkBoxCopyToClipboard;
    private CheckBox checkBoxPreviewLink;
    private CheckBox checkBoxUseDefaultHost;
    private Spinner spinnerDefaultHost;
    private Spinner spinnerTheme;


    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        setContentView(R.layout.settings);

        spinnerTheme = findViewById(R.id.spinnerTheme);
        List<String> themeItems = new ArrayList<>();
        themeItems.add(getString(R.string.checkBoxFollowSystemDarkMode));
        themeItems.add(getString(R.string.dark_theme));
        themeItems.add(getString(R.string.checkBoxLightTheme));
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themeItems);
        spinnerTheme.setAdapter(themeAdapter);

        checkBoxCopyToClipboard = findViewById(R.id.checkBoxCopyToClipboard); //bind copy to clipboard
        checkBoxPreviewLink = findViewById(R.id.checkBoxPreviewLink); //bind copy to clipboard

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true)) {
            spinnerTheme.setSelection(0);
        } else if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) {
            spinnerTheme.setSelection(2);
        } else {
            spinnerTheme.setSelection(1);
        }

        checkBoxCopyToClipboard.setChecked(mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false));
        checkBoxPreviewLink.setChecked(mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false));

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        List<Host> hosts = new ArrayList<>(Arrays.asList(gson.fromJson(json, Host[].class)));

        hostFullAddress.clear();
        List<String> spinnerItems = new ArrayList<>();

        for (Host host : hosts) {
            String fullName = host.host + ":" + host.port;
            if (!host.nickName.equals("")) fullName = host.nickName;
            hostFullAddress.add(host.host + ":" + host.port);
            spinnerItems.add(fullName);
        }

        checkBoxUseDefaultHost = findViewById(R.id.checkBoxUseDefaultHost);
        spinnerDefaultHost = findViewById(R.id.spinnerDefaultHost);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
        spinnerDefaultHost.setAdapter(spinnerAdapter);

        if (hosts.size() > 1) {
            checkBoxUseDefaultHost.setChecked(mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false));
            spinnerDefaultHost.setEnabled(checkBoxUseDefaultHost.isChecked());
            try {
                Log.w("hosts", String.valueOf(hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, "").toString())));
                spinnerDefaultHost.setSelection(hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, "")));
                spinnerDefaultHost.setSelection(1);
            } catch (Exception e) {
                checkBoxUseDefaultHost.setChecked(false);
                spinnerDefaultHost.setSelection(0);
            }
            checkBoxUseDefaultHost.setOnCheckedChangeListener((arg0, isChecked) -> {
                if (isChecked){
                    spinnerDefaultHost.setVisibility(View.VISIBLE);
                }else{
                    spinnerDefaultHost.setVisibility(View.INVISIBLE);
                }

            });
        } else {
            findViewById(R.id.layoutSpinner).setVisibility(View.GONE);
        }

    }

    public void onClick(View v) {
        Intent intentMyActivity = new Intent(Settings.this, MyActivity.class);

        int id = v.getId();
        if (id == R.id.buttonSaveSettings) {  // save button
            SharedPreferences.Editor editor = mSettings.edit();

            if (spinnerTheme.getSelectedItemPosition() == 0){
                editor.putBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true);
            } else if (spinnerTheme.getSelectedItemPosition() == 1){
                editor.putBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false);
                editor.putBoolean(APP_PREFERENCES_THEME_DARK, false);
            } else {
                editor.putBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false);
                editor.putBoolean(APP_PREFERENCES_THEME_DARK, true);
            }

            editor.putBoolean(APP_PREFERENCES_COPY_LINKS, checkBoxCopyToClipboard.isChecked());
            editor.putBoolean(APP_PREFERENCES_PREVIEW_LINKS, checkBoxPreviewLink.isChecked());
            editor.putBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, checkBoxUseDefaultHost.isChecked());
            editor.putString(APP_PREFERENCES_DEFAULT_HOST, hostFullAddress.get(spinnerDefaultHost.getSelectedItemPosition()));
            editor.apply();
            startActivity(intentMyActivity);
            finish();
        } else if (id == R.id.buttonCloseSettings) {  // exit button
            startActivity(intentMyActivity);
            finish();
        } else if (id == R.id.buttonEditHosts) {  // exit button
            Intent intentEditHosts = new Intent(Settings.this, Hosts.class);
            startActivity(intentEditHosts);
            finish();
        }
    }

    public void onBackPressed() { // Back button
        Intent intentMyActivity = new Intent(Settings.this, MyActivity.class);
        startActivity(intentMyActivity);
        finish();
    }

}