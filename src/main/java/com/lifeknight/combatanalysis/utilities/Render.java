package com.lifeknight.combatanalysis.utilities;

import com.lifeknight.combatanalysis.mod.Core;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Render extends Gui {
    public static void drawEmptyBox(int left, int top, int right, int bottom, float[] colors, float alpha, int thickness) {
        drawHorizontalLine(top - thickness + 1, left + 1, right - 1, colors, alpha, thickness);
        drawHorizontalLine(bottom - 1, left + 1, right - 1, colors, alpha, thickness);

        drawVerticalLine(left - thickness + 1, top - thickness + 1, bottom + thickness - 1, colors, alpha, thickness);
        drawVerticalLine(right - 1, top - thickness + 1, bottom + thickness - 1, colors, alpha, thickness);
    }

    public static void drawEmptyBox(int left, int top, int right, int bottom, float[] colors, float alpha) {
        drawEmptyBox(left, top, right, bottom, colors, alpha, 1);
    }

    public static void drawEmptyBox(int left, int top, int right, int bottom, Color color, float alpha) {
        drawEmptyBox(left, top, right, bottom, color, alpha, 1);
    }

    public static void drawEmptyBox(int left, int top, int right, int bottom, Color color, float alpha, int thickness) {
        drawHorizontalLine(top - thickness + 1, left + 1, right - 1, color, alpha, thickness);
        drawHorizontalLine(bottom - 1, left + 1, right - 1, color, alpha, thickness);

        drawVerticalLine(left - thickness + 1, top - thickness + 1, bottom + thickness - 1, color, alpha, thickness);
        drawVerticalLine(right - 1, top - thickness + 1, bottom + thickness - 1, color, alpha, thickness);
    }

    public static void drawHorizontalLine(int y, int startX, int endX, float[] colors, float alpha, int thickness) {
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

    public static void drawHorizontalLine(int y, int startX, int endX, Color color, float alpha, int thickness) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        drawRectangle(startX, y, endX, y + thickness, color, alpha);
    }

    public static void drawVerticalLine(int x, int startY, int endY, Color color, float alpha, int thickness) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        drawRectangle(x, startY, x + thickness, endY, color, alpha);
    }

    public static void drawRectangle(float left, float top, float right, float bottom, Color color, float alpha) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha / 255F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRectangle(float left, float top, float right, float bottom, float[] colors, float alpha) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
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
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRectangle(float left, float top, float right, float bottom, float scale, Color color, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        drawRectangle(left / scale, top / scale, right / scale, bottom / scale, color, alpha);
        GlStateManager.popMatrix();
    }
    public static void glScissor(int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;
        GL11.glScissor(Video.scaleTo1080p(x), Video.scaleTo1080p(Video.getGameHeight() - y - height), Video.scaleTo1080p(width), Video.scaleTo1080p(height));
    }

    public static void drawString(float x, float y, float scale, String text) {
        drawString(x, y, scale, true, text);
    }

    public static void drawString(float x, float y, float scale, boolean dropShadow, String text) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x / scale, y / scale, 0xffffffff, dropShadow);
        GlStateManager.popMatrix();
    }

    public static void drawHorizontallyCenteredString(float x, float y, float scale, boolean dropShadow, String text) {
        drawString(x - scale * Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2F + 1F, y, scale, dropShadow, text);
    }

    public static void drawChromaString(float x, float y, float scale, boolean dropShadow, float speed, String text) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        float x2 = x;
        for (char currentCharacter : text.toCharArray()) {
            long dif = (long) (x2 * 10 - y * 10);
            long l = System.currentTimeMillis() - dif;
            int i = Color.HSBtoRGB(l % (int) speed / speed, 0.8f, 0.8f);
            String currentCharacterString = String.valueOf(currentCharacter);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            fontRenderer.drawString(currentCharacterString, x2 / scale , y / scale, i, dropShadow);
            GlStateManager.popMatrix();
            x2 += fontRenderer.getCharWidth(currentCharacter) * scale;
        }
    }

    public static void drawHorizontallyCenteredChromaString(float x, float y, float scale, boolean dropShadow, float speed, String text) {
        drawChromaString(x - scale * Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2F + 1F, y, scale, dropShadow, speed, text);
    }

    public static void drawString(float x, float y, float scale, boolean dropShadow, boolean chroma, String text) {
        if (chroma) {
            drawChromaString(x, y, scale, dropShadow, 1000F / Core.chromaSpeed.getValue(), text);
            return;
        }
        drawString(x, y, scale, dropShadow, text);
    }

    public static void drawHorizontallyCenteredString(float x, float y, float scale, boolean dropShadow, boolean chroma, String text) {
        if (chroma) {
            drawHorizontallyCenteredChromaString(x, y, scale, dropShadow, 1000F / Core.chromaSpeed.getValue(), text);
            return;
        }
        drawHorizontallyCenteredString(x, y, scale, dropShadow, text);
    }
}
