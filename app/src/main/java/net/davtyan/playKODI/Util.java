package net.davtyan.playKODI;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {

    public static String extractYTId(String ytUrl) {
        String vId = "";
        Pattern pattern = Pattern.compile(".*(?:youtu.be/|v/|u/\\w/|embed/|watch\\?v=)([^#&?]*).*");
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()) {
            vId = matcher.group(1);
        }
        return vId;
    }

    public static String prepareLink(String textToPrepare) {
        String textToPaste = textToPrepare;
        if (textToPaste.contains("%")) {
            try {
                textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                if (textToPaste.contains("%")) {
                    textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //prepare smb link
        if (textToPaste.contains("smb:")) {
            textToPaste = textToPaste.substring(textToPaste.indexOf("smb:"));
            if (textToPaste.contains("?")) {
                textToPaste = textToPaste.substring(0, textToPaste.indexOf("?"));
            }
        }

        //prepare youtube link
        if (textToPaste.contains("youtu")) {
            String youtubeId = extractYTId(textToPaste);
            textToPaste = "plugin://plugin.video.youtube/play/?video_id=" + youtubeId;
        }
        return textToPaste;
    }

    public static void preExecuteMessage(SharedPreferences mSettings,
                                           Object clipboardSystemService,
                                           String textToPaste,
                                           List<Host> hosts,
                                           int position,
                                           Resources mResources,
                                           Context ctx) {
        //copy to clipboard
        if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false) &&
                !textToPaste.contains("plugin://")) {
            ClipboardManager clipboard = (ClipboardManager) clipboardSystemService;
            ClipData clip = ClipData.newPlainText("label", textToPaste);
            Objects.requireNonNull(clipboard).setPrimaryClip(clip);
        }

        //Make toast message
        String fullName = hosts.get(position).host + ":" + hosts.get(position).port;
        if (!hosts.get(position).nickName.equals("")) fullName = hosts.get(position).nickName;
        String toastMessage = mResources.getString(R.string.messageSendingLink) + " " + fullName;

        //preview the link
        if (mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false) &&
                !textToPaste.contains("plugin://")) {
            toastMessage = toastMessage + "\n" + textToPaste;
        }
        Toast.makeText(ctx, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public static String[] prepareRequestParams(String textToPaste,
                                                List<Host> hosts,
                                                int position) {
        String[] requestParams = new String[10];
        requestParams[0] = hosts.get(position).host;
        requestParams[1] = hosts.get(position).port;
        requestParams[2] = hosts.get(position).login;
        requestParams[3] = hosts.get(position).password;
        requestParams[4] = textToPaste;
        return requestParams;
    }

}



