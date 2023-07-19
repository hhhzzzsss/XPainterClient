package com.github.hhhzzzsss.xpainterclient.mixin;

import com.github.hhhzzzsss.xpainterclient.PaintingHandler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void onTick(CallbackInfo ci) {
        PaintingHandler.tick();
    }
}
