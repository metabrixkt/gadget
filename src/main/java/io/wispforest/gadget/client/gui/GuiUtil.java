package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ThrowableUtil;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;
import com.mojang.blaze3d.platform.InputConstants;

public final class GuiUtil {
    private GuiUtil() {

    }

    public static void hoverBlue(LabelComponent label) {
        label.mouseEnter().subscribe(
            () -> label.text(((MutableComponent) label.text()).withStyle(ChatFormatting.BLUE)));

        label.mouseLeave().subscribe(
            () -> label.text(((MutableComponent) label.text()).withStyle(ChatFormatting.WHITE)));
    }

    public static void semiButton(LabelComponent label, Runnable onPressed) {
        hoverBlue(label);
        label.cursorStyle(CursorStyle.HAND);

        label.mouseDown().subscribe((click, doubled) -> {
            if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

            UISounds.playButtonSound();
            onPressed.run();

            return true;
        });
    }

    public static void semiButton(LabelComponent label, BiConsumer<Double, Double> onPressed) {
        hoverBlue(label);
        label.cursorStyle(CursorStyle.HAND);

        label.mouseDown().subscribe((click, doubled) -> {
            if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

            UISounds.playButtonSound();
            onPressed.accept(click.x(), click.y());

            return true;
        });
    }

    public static ParentUIComponent root(UIComponent component) {
        ParentUIComponent root = component instanceof ParentUIComponent parent ? parent : component.parent();

        if (root == null)
            throw new IllegalStateException();

        //noinspection DataFlowIssue
        while (root.hasParent())
            root = root.parent();

        return root;
    }

    public static DropdownComponent contextMenu(UIComponent at, double mouseX, double mouseY) {
        FlowLayout root = (FlowLayout) root(at);
        var dropdown = UIComponents.dropdown(Sizing.content());

        dropdown
            .positioning(Positioning.absolute((int) mouseX + at.x(), (int) mouseY + at.y()));
        // TODO: figure out what to do with this.
//            .zIndex(100);

        ((ParentUIComponent) dropdown.children().get(0)).padding(Insets.of(3));

        dropdown.focusLost().subscribe(() -> dropdown.queue(() -> root.removeChild(dropdown)));

        root.child(dropdown);
        root.focusHandler().focus(dropdown, UIComponent.FocusSource.MOUSE_CLICK);

        return dropdown;
    }

    private static final int INVALID_COLOR = 0xEB1D36;
    private static final int VALID_COLOR = 0x28FFBF;

    public static void textFieldVerifier(EditBox textField, Predicate<String> verifier) {
        textField.setResponder(
            text -> textField.setTextColor(verifier.test(text) ? VALID_COLOR : INVALID_COLOR));
    }

    public static LabelComponent showException(Throwable e) {
        return showExceptionText(ThrowableUtil.throwableToString(e));
    }

    public static LabelComponent showExceptionText(String fullExceptionText) {
        LabelComponent label = UIComponents.label(
            net.minecraft.network.chat.Component.literal(fullExceptionText.replace("\t", "    "))
                .withStyle(ChatFormatting.RED));
        label.horizontalSizing(Sizing.fill(99));
        return label;
    }

    public static void showMonospaceText(FlowLayout container, String all) {
        var lines = all.lines().toList();
        int maxWidth = Integer.toString(lines.size() - 1).length();

        int i = 0;
        for (String line : lines) {
            container.child(UIComponents.label(
                net.minecraft.network.chat.Component.literal("")
                    .append(net.minecraft.network.chat.Component.literal(StringUtils.leftPad(Integer.toString(i), maxWidth) + " ")
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(x -> x.withFont(new FontDescription.Resource(Gadget.id("monocraft")))))
                    .append(net.minecraft.network.chat.Component.literal(line.replace("\t", "    "))
                        .withStyle(x -> x.withFont(new FontDescription.Resource(Gadget.id("monocraft"))))))
                .horizontalSizing(Sizing.fill(99)));

            i++;
        }
    }

    public static FlowLayout hexDump(byte[] bytes, boolean doEllipsis) {
        FlowLayout view = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

        List<UIComponent> expandedChildren = new ArrayList<>();

        int index = 0;
        while (index < bytes.length) {
            StringBuilder line = new StringBuilder();

            line.append(String.format("%04x  ", index));

            int i;
            for (i = 0; i < 16 && index < bytes.length; i++) {
                short b = (short) (bytes[index] & 0xff);

                line.append(String.format("%02x ", b));
                index++;
            }

            line.append("   ".repeat(Math.max(0, 16 - i)));

            for (int j = 0; j < i; j++) {
                short b = (short) (bytes[index - i + j] & 0xff);

                if (b >= 32 && b < 127)
                    line.append((char) b);
                else
                    line.append('.');
            }

            var label = UIComponents.label(net.minecraft.network.chat.Component.literal(line.toString())
                    .withStyle(x -> x.withFont(new FontDescription.Resource(Gadget.id("monocraft")))))
                .margins(Insets.bottom(3));

            if (view.children().size() > 10 && doEllipsis)
                expandedChildren.add(label);
            else
                view.child(label);
        }

        if (expandedChildren.size() > 0) {
            LabelComponent ellipsis = UIComponents.label(net.minecraft.network.chat.Component.literal("..."));

            semiButton(ellipsis, () -> {
                view.removeChild(ellipsis);
                view.children(expandedChildren);
            });

            view.child(ellipsis);
        }

        return view;
    }
}
