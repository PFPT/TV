package com.github.catvod.utils;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Shell {

    private static final String TAG = Shell.class.getSimpleName();

    public static String exec(String... command) {
        if (command == null || command.length == 0) return "";
        try {
            StringBuilder sb = new StringBuilder();
            Process p = new ProcessBuilder(command).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            Logger.t(TAG).d("Shell exit code '%s'", p.waitFor());
            return Util.substring(sb.toString());
        } catch (Exception e) {
            return "";
        }
    }
}