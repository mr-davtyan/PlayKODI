package net.davtyan.playKODI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import static net.davtyan.playKODI.MyActivity.haveToCloseApp;

public class Settings extends AppCompatActivity {

    static final String APP_PREFERENCES = "MySettings";

    static final String APP_PREFERENCES_FIRST_RUN = "Run"; //
    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //
    static final String APP_PREFERENCES_COPY_LINKS = "CopyLinks"; //
    static final String APP_PREFERENCES_PREVIEW_LINKS = "Show Link after sending"; //
    private static SharedPreferences mSettings;
    private CheckBox checkBoxLightTheme;
    private CheckBox checkBoxFollowSystemDark;
    private CheckBox checkBoxCopyToClipboard;
    private CheckBox checkBoxPreviewLink;

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

        checkBoxLightTheme = findViewById(R.id.checkBoxLightTheme); //set the theme
        checkBoxFollowSystemDark = findViewById(R.id.checkBoxFollowSystemDark); //set the theme auto
        checkBoxCopyToClipboard = findViewById(R.id.checkBoxCopyToClipboard); //bind copy to clipboard
        checkBoxPreviewLink = findViewById(R.id.checkBoxPreviewLink); //bind copy to clipboard

        checkBoxLightTheme.setChecked(mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true));

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true)) {
            checkBoxFollowSystemDark.setChecked(true);
            checkBoxLightTheme.setEnabled(false);
        } else {
            checkBoxFollowSystemDark.setChecked(false);
            checkBoxLightTheme.setEnabled(true);
        }

        checkBoxCopyToClipboard.setChecked(mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false));
        checkBoxPreviewLink.setChecked(mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false));

        checkBoxFollowSystemDark.setOnCheckedChangeListener((arg0, isChecked) -> checkBoxLightTheme.setEnabled(!isChecked));
    }

    public void onClick(View v) {
        Intent intentMyActivity = new Intent(Settings.this, MyActivity.class);

        int id = v.getId();
        if (id == R.id.buttonSaveSettings) {  // save button

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_FIRST_RUN, false);
            editor.putBoolean(APP_PREFERENCES_THEME_DARK, checkBoxLightTheme.isChecked());
            editor.putBoolean(APP_PREFERENCES_THEME_DARK_AUTO, checkBoxFollowSystemDark.isChecked());
            editor.putBoolean(APP_PREFERENCES_COPY_LINKS, checkBoxCopyToClipboard.isChecked());
            editor.putBoolean(APP_PREFERENCES_PREVIEW_LINKS, checkBoxPreviewLink.isChecked());

            editor.apply();

            startActivity(intentMyActivity);
            finish();
        } else if (id == R.id.buttonCloseSettings) {  // exit button
            if (!mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true)) {
                startActivity(intentMyActivity);
            } else {
                haveToCloseApp = true;
            }
            finish();
        } else if (id == R.id.buttonEditHosts) {  // exit button
            Intent intentEditHosts = new Intent(Settings.this, Hosts.class);
            startActivity(intentEditHosts);
            finish();
        }
    }

    public void onBackPressed() { // Back button
        if (!mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true)) {
            Intent intentMyActivity = new Intent(Settings.this, MyActivity.class);
            startActivity(intentMyActivity);
        } else {
            haveToCloseApp = true;
        }
        finish();
    }

}