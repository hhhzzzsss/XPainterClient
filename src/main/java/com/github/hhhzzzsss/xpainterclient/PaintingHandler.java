package com.github.hhhzzzsss.xpainterclient;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaintingHandler {
    public static final long POKE_DELAY = 10*1000;

    public static double lastKnownInk = 0.0;
    public static long nextPokeTime = System.currentTimeMillis();

    private static final Pattern INK_PATTERN = Pattern.compile(".*?([\\d\\.]+)/([\\d\\.]+) Ink.*");

    private static int nextClickDelay = 10;

    public static void tick() {
        if (Util.MC.world == null) return;
        if (XPainterClient.pos1 == null || XPainterClient.pos2 == null) return;
        if (XPainterClient.targetFillBlock == null) return;

        long currTime = System.currentTimeMillis();
        if (lastKnownInk < 10.0 && currTime < nextPokeTime) return;

        if (nextClickDelay > 0) {
            nextClickDelay--;
            return;
        } else {
            nextClickDelay = 9;
        }

        nextPokeTime = currTime + POKE_DELAY;

        int x1 = XPainterClient.pos1.getX();
        int y1 = XPainterClient.pos1.getY();
        int z1 = XPainterClient.pos1.getZ();
        int x2 = XPainterClient.pos2.getX();
        int y2 = XPainterClient.pos2.getY();
        int z2 = XPainterClient.pos2.getZ();
        int xs = x1<x2 ? 1 : -1;
        int ys = y1<y2 ? 1 : -1;
        int zs = z1<z2 ? 1 : -1;

        if ((Math.abs(x2-x1)+1)*(Math.abs(y2-y1)+1)*(Math.abs(z2-z1)+1) > 1000000) return;

        for (int x = x1; x != x2+xs; x += xs) {
            for (int y = y1; y != y2+ys; y += ys) {
                for (int z = z1; z != z2+zs; z += zs) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState bs = Util.MC.world.getBlockState(pos);
                    if (bs.isAir()) return;
                    if (!bs.getBlock().equals(XPainterClient.targetFillBlock)) {
                        Vec3d faceCenter = Util.getClosestOpenFacePos(pos, Util.MC.player.getEyePos());
                        if (faceCenter == null) continue;
                        double dx = faceCenter.x - Util.MC.player.getEyePos().x;
                        double dy = faceCenter.y - Util.MC.player.getEyePos().y;
                        double dz = faceCenter.z - Util.MC.player.getEyePos().z;
                        double rHoriz = Math.sqrt(dx*dx + dz*dz);
                        float pitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(dy, rHoriz) * 57.2957763671875)));
                        float yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(dz, dx) * 57.2957763671875) - 90.0f);
                        Util.MC.player.networkHandler.getConnection().send(new PlayerMoveC2SPacket.Full(
                                Util.MC.player.getPos().x, Util.MC.player.getPos().y, Util.MC.player.getPos().z,
                                yaw, pitch,
                                true));
                        Util.MC.player.networkHandler.getConnection().send(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, Integer.MAX_VALUE / 4));
                        return;
                    }
                }
            }
        }
    }

    public static void onOverlaySystemMessage(Text message) {
        String str = message.getString();
        Matcher m = INK_PATTERN.matcher(str);
        if (m.matches()) {
            try {
                lastKnownInk = Double.parseDouble(m.group(1));
                nextPokeTime = System.currentTimeMillis() + POKE_DELAY;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
