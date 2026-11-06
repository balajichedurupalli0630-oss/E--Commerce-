package com.omnicart.service;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLoaderService {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    public static Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            return getDefaultProductImage();
        }

        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }

        try {
            // Check HTTP / HTTPS
            if (path.startsWith("http://") || path.startsWith("https://")) {
                Image img = new Image(path, true);
                CACHE.put(path, img);
                return img;
            }

            // Normalize relative path
            String resourcePath = path;
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }

            // Check /com/omnicart/images/products/... or /com/omnicart/images/...
            String primaryPath = "/com/omnicart/images" + resourcePath;
            InputStream in = ImageLoaderService.class.getResourceAsStream(primaryPath);

            if (in == null) {
                // Try direct resourcePath
                in = ImageLoaderService.class.getResourceAsStream(resourcePath);
            }

            if (in != null) {
                Image img = new Image(in);
                CACHE.put(path, img);
                return img;
            }
        } catch (Exception e) {
            System.err.println("[ImageLoaderService] Failed to load image: " + path + " (" + e.getMessage() + ")");
        }

        return getDefaultProductImage();
    }

    public static Image getDefaultProductImage() {
        String defaultPath = "/com/omnicart/images/products/1g.png";
        if (CACHE.containsKey(defaultPath)) {
            return CACHE.get(defaultPath);
        }
        try {
            InputStream in = ImageLoaderService.class.getResourceAsStream(defaultPath);
            if (in != null) {
                Image img = new Image(in);
                CACHE.put(defaultPath, img);
                return img;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
