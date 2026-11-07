package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import io.wispforest.gadget.client.dump.DumpPrimer;
import io.wispforest.gadget.client.gui.ContextMenuScreens;
import java.net.UnknownHostException;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.EventLoopGroupHolder;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class OnlineServerEntryMixin {
    @Shadow @Final private JoinMultiplayerScreen screen;

    @Shadow @Final private ServerData serverData;

    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract void updateServerList();

    @Shadow protected abstract void refreshStatus();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onRightClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != InputConstants.MOUSE_BUTTON_RIGHT) return;
        if (!Gadget.CONFIG.rightClickDump()) return;

        ContextMenuScreens.contextMenuAt(screen, click.x(), click.y())
            .button(Component.translatable("text.gadget.join_with_dump"), dropdown2 -> {
                DumpPrimer.isPrimed = true;

                this.screen.join(serverData);
            })
            .button(Component.translatable("text.gadget.query_with_dump"), dropdown2 -> {
                ClientPacketDumper.start(false);

                try {
                    this.screen.getPinger().pingServer(
                        this.serverData,
                        () -> this.minecraft.execute(this::updateServerList),
                        () -> {
                            this.serverData
                                .setState(
                                    this.serverData.protocol == SharedConstants.getCurrentVersion().protocolVersion()
                                        ? ServerData.State.SUCCESSFUL
                                        : ServerData.State.INCOMPATIBLE
                                );
                            this.minecraft.execute(this::refreshStatus);
                        },
                        EventLoopGroupHolder.remote(this.minecraft.options.useNativeTransport())
                    );
                } catch (UnknownHostException var2x) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = ServerSelectionListAccessor.getCANT_RESOLVE_TEXT();
                } catch (Exception var3x) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = ServerSelectionListAccessor.getCANT_CONNECT_TEXT();
                }
            });

        cir.setReturnValue(true);
    }
}