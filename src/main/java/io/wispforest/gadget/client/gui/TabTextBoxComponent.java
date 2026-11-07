package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

public class TabTextBoxComponent extends TextBoxComponent {
    public TabTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_TAB) {
            // Pass the event to the root component.
            root().onKeyPress(input);

            return true;
        }

        return super.keyPressed(input);
    }
}
