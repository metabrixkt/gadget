package io.wispforest.gadget.desc;

public record ComplexFieldObject(String className, String tag, boolean isRepeat) implements FieldObject {
    public String text() {
        return className + tag;
    }

    @Override
    public String type() {
        return "complex";
    }

    @Override
    public int color() {
        return 0x0000FF;
    }
}
