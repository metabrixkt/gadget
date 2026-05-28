package io.wispforest.gadget.client.gui.inspector;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;

public class REISupport {
    private REISupport() {

    }

    public static void init() {
        // TODO: depend on shedaniel math.

//        ElementUtils.registerElementSupport(WidgetWithBounds.class, ElementSupport.fromLambda(
//            w -> w.getBounds().x,
//            w -> w.getBounds().y,
//            w -> w.getBounds().width,
//            w -> w.getBounds().height
//        ));

        ElementUtils.registerRootLister(REISupport::tryAddOverlay);
    }

    @SuppressWarnings("unchecked")
    private static void tryAddOverlay(Screen screen, List<ContainerEventHandler> list) {
        try {
            // TODO REI hasn't updated to 26.1 as of 2026-05-28, so our support for it is best-effort for now
            Class<?> runtimeClass = Class.forName("me.shedaniel.rei.api.client.REIRuntime");
            Object runtime = runtimeClass.getMethod("getInstance").invoke(null);

            Optional<?> overlay = (Optional<?>) runtimeClass.getMethod("getOverlay").invoke(runtime);

            if (!(boolean) runtimeClass.getMethod("isOverlayVisible").invoke(runtime)) return;
            if (overlay.isEmpty()) return;
            if (screen != Minecraft.getInstance().screen) return;

            Class<?> screenRegistryClass = Class.forName("me.shedaniel.rei.api.client.registry.screen.ScreenRegistry");
            Object screenRegistry = screenRegistryClass.getMethod("getInstance").invoke(null);
            Iterable<Object> deciders = (Iterable<Object>) screenRegistryClass.getMethod("getDeciders", Screen.class).invoke(screenRegistry, screen);

            boolean succeeded = false;
            for (Object decider : deciders) {
                InteractionResult result = (InteractionResult) decider.getClass().getMethod("shouldScreenBeOverlaid", Screen.class).invoke(decider, screen);

                if (result == InteractionResult.FAIL) {
                    return;
                } else if (result == InteractionResult.SUCCESS) {
                    succeeded = true;
                    break;
                }
            }

            if (!succeeded) return;
            if (overlay.get() instanceof ContainerEventHandler handler) {
                list.add(handler);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError _) {
            // REI is optional and isn't officially updated to 26.1 yet,
            // so incompatibilities with unofficial ports are expected
        }
    }
}
