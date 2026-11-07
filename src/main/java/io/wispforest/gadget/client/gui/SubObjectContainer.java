package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

// Copied from owo-ui's CollapsibleContainer
public class SubObjectContainer extends FlowLayout {
    public static final Surface SURFACE = (ctx, UIcomponent) -> ctx.fill(
        UIcomponent.x() + 5,
        UIcomponent.y(),
        UIcomponent.x() + 6,
        UIcomponent.y() + UIcomponent.height(),
        0x77FFFFFF
    );
    private final Consumer<SubObjectContainer> loader;
    private final Consumer<SubObjectContainer> unloader;

    protected final List<UIComponent> collapsibleChildren = new ArrayList<>();
    protected boolean expanded;

    protected final SpinnyBoiComponent spinnyBoi;

    public SubObjectContainer(Consumer<SubObjectContainer> loader, Consumer<SubObjectContainer> unloader) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.loader = loader;
        this.unloader = unloader;
        this.surface(SURFACE);
        this.padding(Insets.left(15));

        this.allowOverflow(true);

        this.spinnyBoi = new SpinnyBoiComponent();
        this.spinnyBoi
                .cursorStyle(CursorStyle.HAND);

        this.expanded = false;
        this.spinnyBoi.targetRotation = 0;
        this.spinnyBoi.rotation = this.spinnyBoi.targetRotation;

        GuiUtil.hoverBlue(this.spinnyBoi);

        this.spinnyBoi.mouseDown().subscribe((click, doubled) -> {
            this.toggleExpansion();
            UISounds.playInteractionSound();

            return true;
        });
    }

    public UIComponent getSpinnyBoi() {
        return spinnyBoi;
    }

    public void toggleExpansion() {
        if (expanded) {
            this.children.removeAll(collapsibleChildren);
            this.spinnyBoi.targetRotation = 0;
        } else {
            this.children.addAll(this.collapsibleChildren);
            this.spinnyBoi.targetRotation = 90;
        }
        this.updateLayout();

        this.expanded = !this.expanded;

        if (expanded) {
            loader.accept(this);
        } else {
            unloader.accept(this);
        }
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        if (input.key() == InputConstants.KEY_SPACE || input.key() == InputConstants.KEY_RETURN || input.key() == InputConstants.KEY_NUMPADENTER) {
            this.toggleExpansion();

            super.onKeyPress(input);
            return true;
        }

        return super.onKeyPress(input);
    }

    @Override
    public FlowLayout child(UIComponent child) {
        this.collapsibleChildren.add(child);

        if (this.expanded) {
            super.child(child);
        }
        return this;
    }

    @Override
    public FlowLayout child(int index, UIComponent child) {
        this.collapsibleChildren.add(index, child);

        if (this.expanded) {
            super.child(index, child);
        }
        return this;
    }

    @Override
    public FlowLayout removeChild(UIComponent child) {
        this.collapsibleChildren.remove(child);
        return super.removeChild(child);
    }

    @Override
    public FlowLayout clearChildren() {
        this.collapsibleChildren.clear();
        return super.clearChildren();
    }

    public List<UIComponent> collapsibleChildren() {
        return collapsibleChildren;
    }

    protected static class SpinnyBoiComponent extends LabelComponent {

        protected float rotation = 90;
        protected float targetRotation = 90;

        public SpinnyBoiComponent() {
            super(net.minecraft.network.chat.Component.literal(">"));
            this.margins(Insets.horizontal(4));
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += (this.targetRotation - this.rotation) * delta * .65;
        }

        @Override
        public void draw(OwoUIGraphics ctx, int mouseX, int mouseY, float partialTicks, float delta) {
            var matrices = ctx.pose();

            matrices.pushMatrix();
            matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1);
            matrices.rotate((float) Math.toRadians(this.rotation));
            matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1));

            super.draw(ctx, mouseX, mouseY, partialTicks, delta);
            matrices.popMatrix();
        }
    }
}

