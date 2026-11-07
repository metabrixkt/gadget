package io.wispforest.gadget.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.gadget.client.PoseStackLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoseStack.class)
public class PoseStackMixin {
    @Shadow private int lastIndex;

    @Inject(method = "popPose", at = @At("HEAD"), cancellable = true)
    private void onPop(CallbackInfo ci) {
        if (lastIndex == 0) {
            if (PoseStackLogger.tripError((PoseStack) (Object) this, "Tried to pop empty PoseStack")) {
                ci.cancel();
            }
            return;
        }

        PoseStackLogger.logOp((PoseStack)(Object) this, false, lastIndex - 1);
    }

    @Inject(method = "pushPose", at = @At("HEAD"))
    private void onPush(CallbackInfo ci) {
        PoseStackLogger.logOp((PoseStack)(Object) this, true, lastIndex);
    }
}
