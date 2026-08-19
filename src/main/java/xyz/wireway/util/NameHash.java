package xyz.wireway.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Stable 64-bit name hash used to derive numeric ids that match on both peers. */
public final class NameHash {

    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 must be available", e);
        }
    });

    private NameHash() {}

    public static long hash(String name) {
        MessageDigest sha = SHA256.get();
        sha.reset();
        byte[] digest = sha.digest(name.getBytes(StandardCharsets.UTF_8));
        return ByteBuffer.wrap(digest).getLong();
    }
}
