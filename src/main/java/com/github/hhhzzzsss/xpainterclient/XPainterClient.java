package com.github.hhhzzzsss.xpainterclient;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class XPainterClient implements ClientModInitializer {
    public static BlockPos pos1 = null;
    public static BlockPos pos2 = null;
    public static Block targetFillBlock = null;

    @Override
    public void onInitializeClient() {
        CommandProcessor.initCommands();
    }
}
