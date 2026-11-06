package com.omnicart.util;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

public class SvgIcons {

    public static final String HOME = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z M9 22V12h6v10";
    public static final String SEARCH = "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M21 21l-4.35-4.35";
    public static final String BELL = "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0";
    public static final String SHOPPING_CART = "M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6 M9 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M20 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z";
    public static final String SHOPPING_BAG = "M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z M3 6h18 M16 10a4 4 0 0 1-8 0";
    public static final String TRASH = "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2";
    public static final String PLUS = "M12 5v14 M5 12h14";
    public static final String MINUS = "M5 12h14";
    public static final String ARROW_RIGHT = "M5 12h14 M12 5l7 7-7 7";
    public static final String CHECK_CIRCLE = "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3";
    public static final String CREDIT_CARD = "M1 4h22v16H1z M1 10h22";
    public static final String SHIELD_CHECK = "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z M9 12l2 2 4-4";
    public static final String SHIRT = "M20.38 3.46L16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z";
    public static final String USER = "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z";
    public static final String INBOX = "M22 12h-6l-2 3h-4l-2-3H2 M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z";
    public static final String CALENDAR = "M19 4H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z M16 2v4 M8 2v4 M3 10h18";
    public static final String SETTINGS = "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z";
    public static final String MORE_HORIZONTAL = "M12 13a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M19 13a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M5 13a1 1 0 1 0 0-2 1 1 0 0 0 0 2z";
    public static final String ARROW_UP_DOWN = "M7 15l5 5 5-5 M7 9l5-5 5 5";
    public static final String FOOTPRINTS = "M4 16v-2.38C4 11.5 2.97 10.5 3 8c.03-2.72 1.49-6 4.5-6C9.37 2 10 3.8 10 5.5c0 3.11-2 5.5-2 8.5v2 M14 20v-2.38c0-2.12-1.03-3.12-1-5.62.03-2.72 1.49-6 4.5-6 1.87 0 2.5 1.8 2.5 3.5 0 3.11-2 5.5-2 8.5v2";
    public static final String GLASSES = "M2 12a4 4 0 0 1 8 0 4 4 0 0 1-8 0z M14 12a4 4 0 0 1 8 0 4 4 0 0 1-8 0z M10 12h4 M2.5 12L4 7h4 M21.5 12L20 7h-4";
    public static final String BRIEFCASE = "M16 4h-8a2 2 0 0 0-2 2v2H2v11a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8h-4V6a2 2 0 0 0-2-2z M8 6h8v2H8z";
    public static final String VENUS = "M12 15a6 6 0 1 0 0-12 6 6 0 0 0 0 12z M12 15v7 M9 19h6";

    public static SVGPath createIcon(String pathData, double size, Paint color) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(color != null ? color : Color.valueOf("#4b5563"));
        path.setStrokeWidth(1.8);
        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }

    public static SVGPath createFilledIcon(String pathData, double size, Paint color) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.setFill(color != null ? color : Color.valueOf("#111827"));
        path.setStroke(Color.TRANSPARENT);
        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }
}
