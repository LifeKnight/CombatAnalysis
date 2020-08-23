package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Render {
    public static void drawEmptyBox(int left, int top, int right, int bottom, float[] colors, float alpha, int thickness) {
        drawHorizontalLine(left, right + thickness, top, colors, alpha, thickness);
        drawHorizontalLine(left, right + thickness, bottom, colors, alpha, thickness);

        drawVerticalLine(left, top + thickness, bottom, colors, alpha, thickness);
        drawVerticalLine(right, top + thickness, bottom, colors, alpha, thickness);
    }

    public static void drawEmptyBox(int left, int top, int right, int bottom, float[] colors, float alpha) {
        drawEmptyBox(left, top, right, bottom, colors, alpha, 1);
    }

    public static void drawEmptyBox(int left, int top, int right, int bottom, Color color, float alpha, int thickness) {
        drawEmptyBox(left, top, right, bottom, new float[]{color.getRed(), color.getGreen(), color.getBlue()}, alpha);
    }

    public static void drawHorizontalLine(int startX, int endX, int y, float[] colors, float alpha, int thickness) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        drawRectangle(startX, y, endX, y + thickness, colors, alpha);
    }

    public static void drawVerticalLine(int x, int startY, int endY, float[] colors, float alpha, int thickness) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        drawRectangle(x, startY, x + thickness, endY, colors, alpha);
    }

    public static void drawRectangle(int left, int top, int right, int bottom, float[] colors, float alpha) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(colors[0] / 255F, colors[1] / 255F, colors[2] / 255F, alpha / 255F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRectangle(int left, int top, int right, int bottom, Color color, float alpha) {
        drawRectangle(left, top, right, bottom, new float[]{color.getRed(), color.getBlue(), color.getGreen()}, alpha);
    }

    public static void glScissor(int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;
        GL11.glScissor(Video.scaleTo1080p(x), Video.scaleTo1080p(Video.getGameHeight() - y - height), Video.scaleTo1080p(width + 1), Video.scaleTo1080p(height));
    }
}
