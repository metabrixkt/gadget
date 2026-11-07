package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.gadget.Gadget;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract boolean handleDebugKeys(KeyEvent input);

    @ModifyExpressionValue(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"))
    private boolean afterKeyPressed(boolean original, long window, int action, KeyEvent event) {
        var minecraft = Minecraft.getInstance();

        if (original) return true;
        if (!Gadget.CONFIG.debugKeysInScreens()) return false;
        if (!minecraft.options.keyDebugModifier.isDown()) return false;

        return ((KeyboardHandlerMixin)(Object) minecraft.keyboardHandler).handleDebugKeys(event);
    }

    @Inject(method = "handleDebugKeys", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"), cancellable = true)
    private void leaveIfPlayer(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player == null)
            cir.setReturnValue(false);
    }

    @Inject(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/permissions/PermissionCheck;check(Lnet/minecraft/server/permissions/PermissionSet;)Z"), cancellable = true)
    private void leaveOnGameModeSelection(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player == null)
            cir.setReturnValue(false);
    }
}
