package io.wispforest.gadget.desc;

public record BytesFieldObject(String bufferClass, byte[] data) implements FieldObject {
    public String text() {
        return bufferClass.substring(bufferClass.lastIndexOf('.') + 1)
            + ", "
            + data.length
            + " byte"
            + (data.length != 1 ? "s" : "");
    }

    @Override
    public String type() {
        return "bytes";
    }

    @Override
    public int color() {
        return 0x00FF00;
    }
}