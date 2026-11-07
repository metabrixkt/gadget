package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.dump.DumpPrimer;
import io.wispforest.gadget.client.gui.ContextMenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldSelectionListWorldListEntryMixin {
    @Shadow @Final private Screen screen;

    @Shadow public abstract void joinWorld();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onRightClick(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != InputConstants.MOUSE_BUTTON_RIGHT) return;
        if (!Gadget.CONFIG.rightClickDump()) return;

        ContextMenuScreens.contextMenuAt(screen, event.x(), event.y())
                .button(Component.translatable("text.gadget.join_with_dump"), dropdown2 -> {
                    DumpPrimer.isPrimed = true;
                    joinWorld();
                });

        cir.setReturnValue(true);
    }
}
