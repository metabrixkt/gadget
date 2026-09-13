package io.wispforest.gadget.decompile.handle;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.decompile.KnotUtil;
import io.wispforest.gadget.decompile.OpenedURLClassLoader;
import net.minecraft.network.chat.Component;
import org.jetbrains.java.decompiler.main.Fernflower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QuiltflowerHandlerImpl implements io.wispforest.gadget.decompile.QuiltflowerHandler {
    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();
    private final List<String> allClasses;
    final Consumer<Component> logConsumer;

    public QuiltflowerHandlerImpl(Consumer<Component> logConsumer) {
        this.logConsumer = logConsumer;

        allClasses = new ArrayList<>();

        for (Class<?> klass : KnotUtil.INSTRUMENTATION.getInitiatedClasses(Gadget.class.getClassLoader())) {
            if (klass.isHidden()) continue;
            if (klass.isArray()) continue;

            allClasses.add(klass.getName().replace('.', '/'));
        }
    }

    public List<String> allClasses() {
        return allClasses;
    }

    @Override
    public byte[] getClassBytes(String name) {
        if (name.endsWith(".class"))
            name = name.substring(0, name.length() - 6);

        return classBytecodeStash.computeIfAbsent(name.replace('/', '.'),
            className -> KnotUtil.getPostMixinClassByteArray(className, true));
    }

    @Override
    public String decompileClass(Class<?> klass) {
        GadgetResultSaver resultSaver = new GadgetResultSaver();
        Fernflower fernflower = new Fernflower(resultSaver, Map.of("ind", "    "), new GadgetFernflowerLogger(this));

        fernflower.addSource(new ClassContextSource(this, klass));

        if (Gadget.CONFIG.fullDecompilationContext())
            fernflower.addLibrary(new EverythingContextSource(this));

        fernflower.decompileContext();
        fernflower.clearContext();
        return resultSaver.saved;
    }

    static {
        var cl = (OpenedURLClassLoader) QuiltflowerHandlerImpl.class.getClassLoader();

        if (cl == Gadget.class.getClassLoader())
            throw new UnsupportedOperationException("Quiltflower handler was loaded on Knot!");
    }
}
