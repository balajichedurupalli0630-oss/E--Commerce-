package com.omnicart.util;

import java.awt.Desktop;
import java.net.URI;

public class BrowserUtil {
    public static void openUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback for Windows
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            System.err.println("[BrowserUtil] Failed to open URL in browser: " + e.getMessage());
        }
    }
}
