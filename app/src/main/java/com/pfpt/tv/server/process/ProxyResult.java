package com.pfpt.tv.server.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Response;

public final class ProxyResult {

    private final Response response;
    private final int code;
    private final String mime;
    private final InputStream stream;
    private final Map<String, String> headers;

    private ProxyResult(Response response) {
        this.response = response;
        this.code = 0;
        this.mime = null;
        this.stream = null;
        this.headers = null;
    }

    private ProxyResult(int code, String mime, InputStream stream, Map<String, String> headers) {
        this.response = null;
        this.code = code;
        this.mime = mime;
        this.stream = stream;
        this.headers = headers;
    }

    public static ProxyResult parse(Object[] rs) {
        if (rs == null || rs.length == 0) return null;
        if (rs[0] instanceof Response response) return new ProxyResult(response);
        if (rs.length < 3) return null;
        Integer code = asInteger(rs[0]);
        String mime = asString(rs[1]);
        InputStream stream = asStream(rs[2]);
        if (code == null || mime == null || stream == null) return null;
        Map<String, String> headers = null;
        if (rs.length > 3 && rs[3] != null) {
            headers = asHeaders(rs[3]);
            if (headers == null) return null;
        }
        return new ProxyResult(code, mime, stream, headers);
    }

    public boolean isDirectResponse() {
        return response != null;
    }

    public Response getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }

    public String getMime() {
        return mime;
    }

    public InputStream getStream() {
        return stream;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value instanceof String s ? s : null;
    }

    private static InputStream asStream(Object value) {
        if (value instanceof InputStream stream) return stream;
        if (value instanceof byte[] bytes) return new ByteArrayInputStream(bytes);
        return null;
    }

    private static Map<String, String> asHeaders(Object value) {
        if (!(value instanceof Map<?, ?> map)) return null;
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) return null;
            if (!(entry.getValue() instanceof String header)) return null;
            headers.put(key, header);
        }
        return headers;
    }
}
