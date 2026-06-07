package xyz.wireway.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketBuffer {

    private ByteBuffer buf;

    public PacketBuffer() {
        this.buf = ByteBuffer.allocate(256);
    }

    public PacketBuffer(ByteBuffer existing) {
        this.buf = existing;
    }

    private void ensure(int bytes) {
        if (buf.remaining() < bytes) {
            int newCap = Math.max(buf.capacity() * 2, buf.position() + bytes);
            ByteBuffer bigger = ByteBuffer.allocate(newCap);
            buf.flip();
            bigger.put(buf);
            buf = bigger;
        }
    }

    public PacketBuffer writeInt(int v){
        ensure(4); buf.putInt(v);   return this;
    }

    public PacketBuffer writeLong(long v){
        ensure(8); buf.putLong(v);  return this;
    }
    public PacketBuffer writeByte(int v){
        ensure(1); buf.put((byte)v); return this;
    }

    public PacketBuffer writeBoolean(boolean v){
        return writeByte(v ? 1 : 0);
    }

    public PacketBuffer writeFloat(float v){
        ensure(4); buf.putFloat(v);  return this;
    }

    public PacketBuffer writeDouble(double v) {
        ensure(8); buf.putDouble(v); return this;
    }

    public int readInt() {
        return buf.getInt();
    }

    public long readLong(){
        return buf.getLong();
    }

    public byte readByte(){
        return buf.get();
    }

    public boolean readBoolean() {
        return buf.get() != 0;
    }

    public float readFloat(){
        return buf.getFloat();
    }

    public double readDouble(){
        return buf.getDouble();
    }

    public PacketBuffer writeString(String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeInt(b.length);
        ensure(b.length);
        buf.put(b);
        return this;
    }

    public String readString() {
        int len = readInt();
        byte[] b = new byte[len];
        buf.get(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    public <T extends BufferSerializable> PacketBuffer write(T obj) {
        obj.write(this);
        return this;
    }

    public <T> PacketBuffer writeList(List<T> list, BiConsumer<PacketBuffer, T> writer) {
        writeInt(list.size());
        for (T item : list) writer.accept(this, item);
        return this;
    }

    public <T> List<T> readList(Function<PacketBuffer, T> reader) {
        int size = readInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(reader.apply(this));
        return list;
    }

    public <T> PacketBuffer writeOptional(T value, BiConsumer<PacketBuffer, T> writer) {
        if (value == null) { writeBoolean(false); }
        else { writeBoolean(true); writer.accept(this, value); }
        return this;
    }

    public <T> T readOptional(Function<PacketBuffer, T> reader) {
        return readBoolean() ? reader.apply(this) : null;
    }

    public ByteBuffer toByteBuffer() {
        buf.flip();
        return buf;
    }

    public <T extends BufferSerializable> PacketBuffer writeList(List<T> list) {
        writeInt(list.size());
        for (T item : list) item.write(this);
        return this;
    }

}
