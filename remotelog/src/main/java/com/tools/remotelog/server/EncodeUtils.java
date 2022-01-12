package com.tools.remotelog.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

class EncodeUtils {

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static byte[] sha1(String... strs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (String str : strs) {
                digest.update(str.getBytes(DEFAULT_CHARSET));
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> parseUrlQueryString(String s) {
        if (s == null) return new HashMap<>(0);

        HashMap<String, String> map1 = new HashMap<>();
        int p = 0;
        while (p < s.length()) {
            int p0 = p;
            while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&') p++;
            String name = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '=') p++;
            p0 = p;
            while (p < s.length() && s.charAt(p) != '&') p++;
            String value = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '&') p++;
            if(value!=null){
                map1.put(name, value.trim());
            }

        }
        return map1;
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error in urlDecode.", e);
        }
    }

}
