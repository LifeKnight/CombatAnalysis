package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.Minecraft;

public class Video {
    public static int scale(int toScale) {
        switch (Minecraft.getMinecraft().gameSettings.guiScale) {
            case 0:
                return (int) ((toScale * 2) / (double) getScaleFactor());
            case 1: {
                return toScale * 2;
            }
            case 2: {
                return toScale;
            }
            default: {
                return (int) (toScale / 1.5);
            }
        }
    }

    public static int scaleTo1080p(int toScale) {
        int scale;
        return ((scale = Minecraft.getMinecraft().gameSettings.guiScale) == 0) ? (int) (toScale * (double) getScaleFactor()) : toScale * scale;
    }

    public static int get2ndPanelCenter() {
        return getScaledHeight(300) + (getGameWidth() - getScaledWidth(300)) / 2;
    }

    public static int getSupposedWidth() {
        return 1920 / (Minecraft.getMinecraft().gameSettings.guiScale == 0 ? getScaleFactor() : Minecraft.getMinecraft().gameSettings.guiScale);
    }

    public static int getSupposedHeight() {
        return 1080 / (Minecraft.getMinecraft().gameSettings.guiScale == 0 ? getScaleFactor() : Minecraft.getMinecraft().gameSettings.guiScale);
    }

    public static int getScaledWidth(int widthIn) {
        return scale((int) (widthIn * ((double) getGameWidth() / (double) getSupposedWidth())));
    }

    public static int getScaledHeight(int heightIn) {
        return scale((int) (heightIn * ((double) getGameHeight() / (double) getSupposedHeight())));
    }

    private static int getScaleFactor() {
        int scaledWidth = Minecraft.getMinecraft().displayWidth;
        int scaledHeight = Minecraft.getMinecraft().displayHeight;
        int scaleFactor = 1;
        boolean flag = Minecraft.getMinecraft().isUnicode();
        int i = Minecraft.getMinecraft().gameSettings.guiScale;

        if (i == 0) {
            i = 1000;
        }

        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor;
        }

        return scaleFactor;
    }

    public static int getGameWidth() {
        if (Minecraft.getMinecraft().gameSettings.guiScale != 0) {
            return Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().gameSettings.guiScale;
        }
        return (int) (Math.ceil(Minecraft.getMinecraft().displayWidth / (double) getScaleFactor()));
    }

    public static int getGameHeight() {
        if (Minecraft.getMinecraft().gameSettings.guiScale != 0) {
            return Minecraft.getMinecraft().displayHeight / Minecraft.getMinecraft().gameSettings.guiScale;
        }
        return (int) (Math.ceil(Minecraft.getMinecraft().displayHeight / (double) getScaleFactor()));
    }

    public static float scaleFrom1080pWidth(float widthIn) {
        float i = widthIn / getScaleFactor();
        return (float) (i * (getGameWidth() / (double) getSupposedWidth()));
    }

    public static float scaleFrom1080pHeight(float heightIn) {
        float i = heightIn / getScaleFactor();
        return (float) (i * (getGameHeight() / (double) getSupposedHeight()));
    }

    public static float scaleTo1080pWidth(float widthIn) {
        float i = widthIn * getScaleFactor();
        return (float) (i * (getSupposedWidth() / (double) getGameWidth()));
    }

    public static float scaleTo1080pHeight(float heightIn) {
        float i = heightIn * getScaleFactor();
        return (float) (i * (getSupposedHeight() / (double) getGameHeight()));
    }
}
