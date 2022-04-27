package net.davtyan.playKODI;

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
}