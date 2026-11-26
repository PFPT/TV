package com.pfpt.tv.utils;

import java.io.File;
import java.io.IOException;

public final class StoragePaths {

    private StoragePaths() {
    }

    public static File confine(File base, String child) {
        if (base == null) return null;
        try {
            File root = base.getCanonicalFile();
            String relative = normalize(child);
            File target = relative.isEmpty() ? root : new File(root, relative).getCanonicalFile();
            if (!contains(root, target)) return null;
            return target;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean contains(File root, File target) {
        if (root == null || target == null) return false;
        String base = root.getPath();
        String path = target.getPath();
        return path.equals(base) || path.startsWith(base + File.separator);
    }

    public static String normalize(String child) {
        if (child == null) return "";
        String value = child.trim().replace('\\', '/');
        if (value.regionMatches(true, 0, "file:", 0, 5)) {
            value = value.substring(5);
        }
        while (value.startsWith("./")) value = value.substring(2);
        while (value.startsWith("/")) value = value.substring(1);
        return value;
    }

    public static String js(String value) {
        if (value == null) return "";
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '<' -> builder.append("\\u003c");
                case '>' -> builder.append("\\u003e");
                case '&' -> builder.append("\\u0026");
                case '\u2028' -> builder.append("\\u2028");
                case '\u2029' -> builder.append("\\u2029");
                default -> builder.append(c);
            }
        }
        return builder.toString();
    }
}
