package com.pfpt.tv.server;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LanToken {

    public static final String CLIENT = "pfpt";
    public static final String CLIENT_HEADER = "x-tv-client";
    public static final String TOKEN_HEADER = "x-tv-token";
    public static final String QUERY = "token";

    private static final String COOKIE = "tv_token";
    private static final ConcurrentHashMap<String, String> peers = new ConcurrentHashMap<>();

    private LanToken() {
    }

    public static String generate() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) builder.append(String.format("%02x", b));
        return builder.toString();
    }

    public static void pair(String uuid, String token) {
        if (uuid == null || uuid.isEmpty() || token == null || token.isEmpty()) return;
        peers.put(uuid, token);
    }

    public static String peer(String uuid) {
        return uuid == null ? null : peers.get(uuid);
    }

    public static String action(String ip, String uuid, String suffix) {
        String base = (ip == null ? "" : ip) + (suffix == null ? "" : suffix);
        return with(base, peer(uuid));
    }

    public static boolean accept(String expected, Map<String, String> params, Map<String, String> headers) {
        return matches(expected, presented(params, headers));
    }

    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null || expected.isEmpty() || actual.isEmpty()) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    public static String presented(Map<String, String> params, Map<String, String> headers) {
        String query = params == null ? null : params.get(QUERY);
        if (query != null && !query.isEmpty()) return query;
        if (headers == null) return null;
        String header = header(headers, TOKEN_HEADER);
        if (header != null && !header.isEmpty()) return header;
        return cookie(header(headers, "cookie"), COOKIE);
    }

    public static String cookieHeader(String token) {
        return COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax";
    }

    public static String with(String url, String token) {
        if (url == null || token == null || token.isEmpty() || url.contains(QUERY + "=")) return url;
        return url + (url.contains("?") ? "&" : "?") + QUERY + "=" + token;
    }

    public static String origin(String url) {
        if (url == null) return "";
        int query = url.indexOf('?');
        String base = query >= 0 ? url.substring(0, query) : url;
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base;
    }

    public static String query(String url, String name) {
        if (url == null || name == null) return null;
        int index = url.indexOf('?');
        if (index < 0 || index == url.length() - 1) return null;
        for (String part : url.substring(index + 1).split("&")) {
            int eq = part.indexOf('=');
            if (eq <= 0) continue;
            if (!name.equals(decode(part.substring(0, eq)))) continue;
            return decode(part.substring(eq + 1));
        }
        return null;
    }

    private static String header(Map<String, String> headers, String name) {
        String value = headers.get(name);
        if (value != null) return value;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
        }
        return null;
    }

    private static String cookie(String header, String name) {
        if (header == null || header.isEmpty()) return null;
        for (String part : header.split(";")) {
            String item = part.trim();
            int eq = item.indexOf('=');
            if (eq <= 0) continue;
            if (name.equals(item.substring(0, eq).trim())) return item.substring(eq + 1).trim();
        }
        return null;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
