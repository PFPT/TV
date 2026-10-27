package com.pfpt.tv.utils;

public class Github {

    public static final String URL = "https://github.com/PFPT/TV/releases/latest/download";

    private static String getUrl(String name) {
        return URL + "/" + name;
    }

    public static String getJson(String name) {
        return getUrl(name + ".json");
    }

    public static String getApk(String name) {
        return getUrl(name + ".apk");
    }
}
