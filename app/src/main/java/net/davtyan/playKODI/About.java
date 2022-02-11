package net.davtyan.playKODI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class About extends Activity {

    static final String APP_PREFERENCES = "MySettings";
    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        setTheme(R.style.Theme_Transparent);

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
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

        setContentView(R.layout.about);
        String version = "";
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String textVersionToPaste = getResources().getString(R.string.textAboutVersion) + " " + version;
        TextView textVersion = findViewById(R.id.textVersion);
        textVersion.setText(textVersionToPaste); // insert text version
    }

    public void onClick(View view) {
        if (view.getId() == R.id.buttonCloseAbout) { // exit button
            finish();
        }
    }
}
