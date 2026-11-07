package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.client.DialogUtil;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.util.Observable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

public class SaveFilePathComponent extends FlowLayout {
    private final String title;
    private final Observable<String> path;
    private final LabelComponent label;
    private List<String> patterns = new ArrayList<>();
    private String filterDescription = null;
    private boolean dialogOpen;

    public SaveFilePathComponent(String title, String defaultPath) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.title = title;
        this.path = Observable.of(defaultPath);

        this.label = UIComponents.label(Component.literal("breh"));

        this.configureLabel(defaultPath);
        path.observe(this::configureLabel);

        child(label);

        padding(Insets.of(2));
        surface(Surface.flat(0x55555555));

        mouseEnter().subscribe(() -> surface(Surface.flat(0xaaaaaaaa)));
        mouseLeave().subscribe(() -> surface(Surface.flat(0x55555555)));

        cursorStyle(CursorStyle.HAND);
        label.cursorStyle(CursorStyle.HAND);
    }

    private void configureLabel(String newPath) {
        int neededSlash = newPath.lastIndexOf('/', 40);

        if (neededSlash != -1) {
            newPath = "..." + newPath.substring(neededSlash);
        }

        this.label.text(Component.literal(newPath));
    }

    public List<String> patterns() {
        return patterns;
    }

    public SaveFilePathComponent patterns(List<String> patterns) {
        this.patterns = patterns;
        return this;
    }

    public SaveFilePathComponent pattern(String pattern) {
        patterns.add(pattern);
        return this;
    }

    public String filterDescription() {
        return filterDescription;
    }

    public SaveFilePathComponent filterDescription(String description) {
        this.filterDescription = description;
        return this;
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            if (dialogOpen) return true;
            dialogOpen = true;
            DialogUtil.saveFileDialog(
                title,
                path.get(),
                patterns.isEmpty() ? null : patterns,
                filterDescription
            ).whenComplete((selected, _) -> {
                dialogOpen = false;
                if (selected != null) this.path.set(selected);
            });

            return true;
        }

        return super.onMouseDown(click, doubled);
    }

    public Observable<String> path() {
        return path;
    }
}
