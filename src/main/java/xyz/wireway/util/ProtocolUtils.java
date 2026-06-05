package xyz.wireway.util;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ProtocolUtils {

    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 must be available", e);
        }
    });

    public static long packetIdHash(String name) {
        MessageDigest sha = SHA256.get();
        sha.reset();
        byte[] digest = sha.digest(name.getBytes(StandardCharsets.UTF_8));
        return ByteBuffer.wrap(digest).getLong();
    }


    public static boolean canRead(ByteBuffer byteBuffer){
        byteBuffer = byteBuffer.duplicate();
        int totalLength = byteBuffer.remaining();
        if(totalLength==0) return false;
        try {
            int frameLength = VarInt.readVarIntSafe(byteBuffer);
            return totalLength >= (frameLength + VarInt.sizeOf(frameLength));
        } catch (BufferOverflowException e){
            return false;
        }
    }

    public static boolean canRead(ComposedBuffer composedBuffer){
        int totalLength = composedBuffer.remaining();
        if(totalLength==0) return false;
        int peekLength = Math.max(totalLength, 5);
        try {
            int frameLength = VarInt.readVarIntSafe(composedBuffer.peek(peekLength));
            return totalLength >= (frameLength + VarInt.sizeOf(frameLength));
        } catch (BufferOverflowException e){
            return false;
        }
    }


}
