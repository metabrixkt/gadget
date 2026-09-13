package io.wispforest.gadget.decompile;

public interface QuiltflowerHandler {
    byte[] getClassBytes(String name);

    String decompileClass(Class<?> klass);
}
