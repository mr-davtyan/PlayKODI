package net.davtyan.playKODI;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class Hosts extends AppCompatActivity {
    ListView list;

    String[] maintitle ={
            "Title 1","Title 2",
            "Title 3","Title 4",
            "Title 5",
    };

    String[] subtitle ={
            "Sub Title 1","Sub Title 2",
            "Sub Title 3","Sub Title 4",
            "Sub Title 5",
    };

    Integer[] imgid={    };



    private static SharedPreferences mSettings;
    static final String APP_PREFERENCES = "MySettings";

    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //

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

        HostAdapter adapter=new HostAdapter(this, maintitle, subtitle,imgid);
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            if(position == 0) {
                Toast.makeText(getApplicationContext(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonAddHost) {
            Intent intentHostEditor = new Intent(Hosts.this, HostEditor.class);
            intentHostEditor.putExtra("ID", -1);
            startActivity(intentHostEditor);
//            todo launch hosteditor with position len of hosts
        } else if (id == R.id.buttonCloseHostList) { // button Send to KODI
            finish();
        }

    }
}