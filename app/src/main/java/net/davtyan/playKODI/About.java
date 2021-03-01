package net.davtyan.playKODI;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class About extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
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
        textVersion.setText(textVersionToPaste);// paste text version
    }

    public void onClick(View view) {
        if (view.getId() == R.id.buttonCloseAbout) { // exit button
            finish();
        }
    }
}
