package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;

public class SidebarBuilder {
    private final FlowLayout layout = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

    {
        layout
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(2));
    }

    public FlowLayout layout() {
        return layout;
    }

    public void button(String translationKeyBase, OnPressHandler handler) {
        button(Component.translatable(translationKeyBase), Component.translatable(translationKeyBase + ".tooltip"), handler);
    }

    public void button(Component icon, @Nullable Component tooltip, OnPressHandler handler) {
        Button button = new Button(icon, tooltip);

        button.mouseDown().subscribe((click, doubled) -> {
            if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            handler.onPress((int) click.x(), (int) click.y());

            return true;
        });

        layout.child(button);
    }

    public static class Button extends FlowLayout {
        private final LabelComponent iconLabel;

        public Button(Component icon, Component tooltip) {
            super(Sizing.fixed(16), Sizing.fixed(16), Algorithm.VERTICAL);

            child((iconLabel = UIComponents.label(icon))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );
            cursorStyle(CursorStyle.HAND);

            if (tooltip != null)
                tooltip(tooltip);

            mouseEnter().subscribe(
                () -> surface(Surface.flat(0x80ffffff)));

            mouseLeave().subscribe(
                () -> surface(Surface.BLANK));
        }

        public Button icon(Component icon) {
            iconLabel.text(icon);

            return this;
        }
    }

    public interface OnPressHandler {
        void onPress(int mouseX, int mouseY);
    }
}
