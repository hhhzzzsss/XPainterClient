package com.github.hhhzzzsss.xpainterclient;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class BoxRenderer {
    public static VertexBuffer boxBuffer;

    public static void render(MatrixStack matrices) {
        if (boxBuffer == null) {
            setupBoxBuffer();
        }

        if (XPainterClient.pos1 == null || XPainterClient.pos2 == null) {
            return;
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        matrices.push();

        Camera camera = Util.MC.getBlockEntityRenderDispatcher().camera;
        if (camera == null) return;
        Vec3d camPos = camera.getPos();
//        BlockPos blockPos = camera.getBlockPos();
//        int regionX = (blockPos.getX() >> 9) * 512;
//        int regionZ = (blockPos.getZ() >> 9) * 512;

        int minX = Math.min(XPainterClient.pos1.getX(), XPainterClient.pos2.getX());
        int maxX = Math.max(XPainterClient.pos1.getX(), XPainterClient.pos2.getX())+1;
        int minY = Math.min(XPainterClient.pos1.getY(), XPainterClient.pos2.getY());
        int maxY = Math.max(XPainterClient.pos1.getY(), XPainterClient.pos2.getY())+1;
        int minZ = Math.min(XPainterClient.pos1.getZ(), XPainterClient.pos2.getZ());
        int maxZ = Math.max(XPainterClient.pos1.getZ(), XPainterClient.pos2.getZ())+1;

        matrices.translate(minX - camPos.x, minY - camPos.y, minZ - camPos.z);
        matrices.scale((float)(maxX - minX), (float)(maxY - minY), (float)(maxZ - minZ));
//
        RenderSystem.setShaderColor(0.5f, 1.0f, 0.5f, 0.5f);

        Util.drawOutlinedBox(new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), matrices);

        matrices.pop();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void setupBoxBuffer() {
        boxBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(0.0, 0.0, 0.0).next();
        bufferBuilder.vertex(1.0, 0.0, 0.0).next();

        bufferBuilder.vertex(1.0, 0.0, 0.0).next();
        bufferBuilder.vertex(1.0, 0.0, 1.0).next();

        bufferBuilder.vertex(1.0, 0.0, 1.0).next();
        bufferBuilder.vertex(0.0, 0.0, 1.0).next();

        bufferBuilder.vertex(0.0, 0.0, 1.0).next();
        bufferBuilder.vertex(0.0, 0.0, 0.0).next();

        bufferBuilder.vertex(0.0, 0.0, 0.0).next();
        bufferBuilder.vertex(0.0, 1.0, 0.0).next();

        bufferBuilder.vertex(1.0, 0.0, 0.0).next();
        bufferBuilder.vertex(1.0, 1.0, 0.0).next();

        bufferBuilder.vertex(1.0, 0.0, 1.0).next();
        bufferBuilder.vertex(1.0, 1.0, 1.0).next();

        bufferBuilder.vertex(0.0, 0.0, 1.0).next();
        bufferBuilder.vertex(0.0, 1.0, 1.0).next();

        bufferBuilder.vertex(0.0, 1.0, 0.0).next();
        bufferBuilder.vertex(1.0, 1.0, 0.0).next();

        bufferBuilder.vertex(1.0, 1.0, 0.0).next();
        bufferBuilder.vertex(1.0, 1.0, 1.0).next();

        bufferBuilder.vertex(1.0, 1.0, 1.0).next();
        bufferBuilder.vertex(0.0, 1.0, 1.0).next();

        bufferBuilder.vertex(0.0, 1.0, 1.0).next();
        bufferBuilder.vertex(0.0, 1.0, 0.0).next();

        BufferBuilder.BuiltBuffer buffer = bufferBuilder.end();

        boxBuffer.bind();
        boxBuffer.upload(buffer);
        VertexBuffer.unbind();
    }
}
