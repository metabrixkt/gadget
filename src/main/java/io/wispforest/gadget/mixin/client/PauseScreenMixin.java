package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.GadgetScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
        ordinal = 1))
    private void addGadgetButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        if (!Gadget.CONFIG.menuButtonEnabled()) return;

        // Join the row so its layout reserves space alongside other mods' icons.
        iconButtonRow.addChild(
            Button
                .builder(
                    Component.translatable("text.gadget.menu_button"),
                    _ -> this.minecraft.gui.setScreen(new GadgetScreen(this))
                )
                .width(20)
                .build()
        );
    }
}
