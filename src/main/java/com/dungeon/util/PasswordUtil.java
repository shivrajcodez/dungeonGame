package com.dungeon.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtil {
    private static final SecureRandom RNG = new SecureRandom();

    public static String hash(String plain) {
        try {
            byte[] salt = new byte[16];
            RNG.nextBytes(salt);
            String saltHex = hex(salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return saltHex + ":" + hex(md.digest(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static boolean verify(String plain, String stored) {
        try {
            String[] p = stored.split(":");
            if (p.length != 2) return false;
            byte[] salt = fromHex(p[0]);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return p[1].equals(hex(md.digest(plain.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) { return false; }
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    private static byte[] fromHex(String h) {
        byte[] out = new byte[h.length()/2];
        for (int i = 0; i < out.length; i++)
            out[i] = (byte) Integer.parseInt(h.substring(i*2, i*2+2), 16);
        return out;
    }
}
