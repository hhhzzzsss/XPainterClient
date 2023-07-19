package com.github.hhhzzzsss.xpainterclient.mixin;

import com.github.hhhzzzsss.xpainterclient.CommandProcessor;
import com.github.hhhzzzsss.xpainterclient.PaintingHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "sendChatMessage(Ljava/lang/String;)V", cancellable=true)
    private void onSendChatMessage(String content, CallbackInfo ci) {
        boolean isCommand = CommandProcessor.processChatMessage(content);
        if (isCommand) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "onGameMessage(Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;)V")
    public void onOnGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (packet.overlay()) {
            PaintingHandler.onOverlaySystemMessage(packet.content());
        }
    }
}
