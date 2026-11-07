package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.TabTextBoxComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

public class KeyAdderWidget extends FlowLayout {
    private final NbtDataIsland island;
    private final NbtPath parentPath;
    private final TagType<?> type;
    private final Predicate<String> nameVerifier;

    private final TextBoxComponent nameField;
    private final TextBoxComponent valueField;
    private boolean wasMounted = false;

    public KeyAdderWidget(NbtDataIsland island, NbtPath parentPath, TagType<?> type, Predicate<String> nameVerifier) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

        this.island = island;
        this.parentPath = parentPath;
        this.type = type;
        this.nameVerifier = nameVerifier;

        child(UIComponents.label(island.typeText(type, "")
            .append(" ")));
        child((this.nameField = new TabTextBoxComponent(Sizing.fixed(75)))
            .verticalSizing(Sizing.fixed(8)));

        if (typeNeedsValue(type)) {
            child(UIComponents.label(Component.nullToEmpty(" = ")));

            child((this.valueField = new TabTextBoxComponent(Sizing.fixed(75)))
                .verticalSizing(Sizing.fixed(8)));
        } else if (typeNeedsSize(type)) {
            child(UIComponents.label(Component.nullToEmpty("["))
                .margins(Insets.horizontal(2)));

            child((this.valueField = new TabTextBoxComponent(Sizing.fixed(50)))
                .verticalSizing(Sizing.fixed(8)));

            child(UIComponents.label(Component.nullToEmpty("]"))
                .margins(Insets.horizontal(2)));
        } else {
            this.valueField = null;
        }

        this.nameField.keyPress().subscribe(this::onNameFieldKeyPressed);
        this.nameField.focusLost().subscribe(this::onFieldFocusLost);

        GuiUtil.textFieldVerifier(this.nameField, nameVerifier);

        if (this.valueField != null) {
            this.valueField.keyPress().subscribe(this::onValueFieldKeyPressed);
            this.valueField.focusLost().subscribe(this::onFieldFocusLost);
            GuiUtil.textFieldVerifier(this.valueField, this::verifyValue);
        }
    }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        super.mount(parent, x, y);

        if (!wasMounted) {
            wasMounted = true;

            island.focusHandler().focus(nameField, FocusSource.MOUSE_CLICK);
            nameField.setFocused(true);
        }
    }

    private void onFieldFocusLost() {
        Minecraft.getInstance().schedule(() -> {
            if (focusHandler() == null) return;

            var newFocused = focusHandler().focused();

            if (newFocused == nameField || newFocused == valueField) return;

            parent().removeChild(this);
        });
    }

    private void commit() {
        if (!nameVerifier.test(nameField.getValue())) return;
        if (valueField != null && !verifyValue(valueField.getValue())) return;

        Tag element;

        if (type == CompoundTag.TYPE)
            element = new CompoundTag();
        else if (type == ListTag.TYPE)
            element = new ListTag();
        else if (type == ByteArrayTag.TYPE)
            element = new ByteArrayTag(new byte[Integer.parseInt(valueField.getValue())]);
        else if (type == IntArrayTag.TYPE)
            element = new IntArrayTag(new int[Integer.parseInt(valueField.getValue())]);
        else if (type == LongArrayTag.TYPE)
            element = new LongArrayTag(new long[Integer.parseInt(valueField.getValue())]);
        else if (type == StringTag.TYPE)
            element = StringTag.valueOf(valueField.getValue());
        else if (type == ByteTag.TYPE)
            element = ByteTag.valueOf(Byte.parseByte(valueField.getValue()));
        else if (type == ShortTag.TYPE)
            element = ShortTag.valueOf(Short.parseShort(valueField.getValue()));
        else if (type == IntTag.TYPE)
            element = IntTag.valueOf(Integer.parseInt(valueField.getValue()));
        else if (type == LongTag.TYPE)
            element = LongTag.valueOf(Long.parseLong(valueField.getValue()));
        else if (type == FloatTag.TYPE)
            element = FloatTag.valueOf(Float.parseFloat(valueField.getValue()));
        else if (type == DoubleTag.TYPE)
            element = DoubleTag.valueOf(Double.parseDouble(valueField.getValue()));
        else if (type == EndTag.TYPE)
            element = EndTag.INSTANCE;
        else
            throw new IllegalStateException("Unknown NbtType");

        NbtPath path = parentPath.then(nameField.getValue());
        path.add(island.data, element);
        island.makeComponent(path, element);
        island.reloader.accept(island.data);

        parent().removeChild(this);
    }

    private boolean onNameFieldKeyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_RETURN) {
            if (valueField == null) {
                commit();
            } else {
                island.focusHandler().focus(valueField, FocusSource.MOUSE_CLICK);
                valueField.setFocused(true);
            }

            return true;
        }

        return false;
    }

    private boolean onValueFieldKeyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_RETURN) {
            commit();

            return true;
        }

        return false;
    }

    private static boolean typeNeedsValue(TagType<?> type) {
        return type != EndTag.TYPE
            && type != CompoundTag.TYPE
            && type != ListTag.TYPE
            && type != ByteArrayTag.TYPE
            && type != IntArrayTag.TYPE
            && type != LongArrayTag.TYPE;
    }

    private static boolean typeNeedsSize(TagType<?> type) {
        return type == ByteArrayTag.TYPE
            || type == IntArrayTag.TYPE
            || type == LongArrayTag.TYPE;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean verifyValue(String value) {
        if (type == ByteArrayTag.TYPE
         || type == IntArrayTag.TYPE
         || type == LongArrayTag.TYPE) {
            return tryRun(() -> Integer.parseInt(value));
        } else if (type == ByteTag.TYPE) {
            return tryRun(() -> Byte.parseByte(value));
        } else if (type == ShortTag.TYPE) {
            return tryRun(() -> Short.parseShort(value));
        } else if (type == IntTag.TYPE) {
            return tryRun(() -> Integer.parseInt(value));
        } else if (type == LongTag.TYPE) {
            return tryRun(() -> Long.parseLong(value));
        } else if (type == FloatTag.TYPE) {
            return tryRun(() -> Float.parseFloat(value));
        } else if (type == DoubleTag.TYPE) {
            return tryRun(() -> Double.parseDouble(value));
        } else {
            return true;
        }
    }

    private boolean tryRun(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
