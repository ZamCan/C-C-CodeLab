package com.zamcan.codelab;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Stable, owner-scoped Appwrite row IDs; never derive access rights from this ID. */
public final class ProgressRowIds {
    private ProgressRowIds() {}
    public static String forLesson(String course, String lesson) {
        if (!valid(course) || !valid(lesson))
            throw new IllegalArgumentException("Invalid lesson key");
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256")
                .digest((course+":"+lesson).getBytes(StandardCharsets.UTF_8));
            StringBuilder id=new StringBuilder("p_");
            for(int i=0;i<16;i++){
                int b=digest[i]&255;
                id.append(Character.forDigit(b>>>4,16));
                id.append(Character.forDigit(b&15,16));
            }
            return id.toString();
        } catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private static boolean valid(String s) {
        return s!=null && s.length()>0 && s.length()<=100 && s.matches("[A-Za-z0-9_-]+");
    }
}
