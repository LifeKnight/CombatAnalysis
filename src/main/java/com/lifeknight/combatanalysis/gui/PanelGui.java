package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.gui.components.LifeKnightTextField;
import com.lifeknight.combatanalysis.gui.components.ScrollBar;
import com.lifeknight.combatanalysis.utilities.Render;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.WHITE;

public class PanelGui extends GuiScreen {
    protected String name = "";
    protected final List<GuiPanel> guiPanels = new ArrayList<>();
    protected final List<GuiPanel> visibleGuiPanels = new ArrayList<>();
    protected ScrollBar scrollBar;
    protected ScrollBar.HorizontalScrollBar horizontalScrollBar;
    protected LifeKnightTextField searchField;
    protected int panelHeight = 0;
    protected int panelWidth = 0;
    protected boolean uniformHeight = true;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        double scale = 5 * (Video.getGameWidth() / (double) Video.getSupposedWidth());
        GlStateManager.scale(scale, scale, scale);
        this.fontRendererObj.drawString(this.name, 1, 2, 0xffffffff, true);
        GlStateManager.popMatrix();

        Render.drawHorizontalLine(0, this.width, 55, new float[]{255, 255, 255}, 255F, 2);

        for (GuiPanel guiPanel : this.visibleGuiPanels) {
            guiPanel.drawPanel();
        }

        this.searchField.drawTextBoxAndName();

        if (this.visibleGuiPanels.size() != 0) {
            int j = Mouse.getDWheel() / 7;
            if (j != 0 && this.notHoveringOver(mouseX, mouseY, j)) {
                if (((j > 0) && this.scrollBar.yPosition > 0) || ((j < 0) && this.scrollBar.yPosition + this.scrollBar.height < super.height)) {

                    for (GuiPanel guiPanel : this.visibleGuiPanels) {
                        guiPanel.updateOriginals();
                    }

                    while (j > 0 && this.visibleGuiPanels.get(0).yPosition + j > 65) {
                        j--;
                    }

                    int lastPanelEndY = 0;
                    for (GuiPanel guiPanel : this.visibleGuiPanels) {
                        if (guiPanel.originalYPosition + guiPanel.height > lastPanelEndY) {
                            lastPanelEndY = guiPanel.originalYPosition + guiPanel.height;
                        }
                    }
                    while (j < 0 && lastPanelEndY + j < super.height - 10) {
                        j++;
                    }

                    for (GuiPanel guiPanel : this.visibleGuiPanels) {
                        guiPanel.yPosition = guiPanel.originalYPosition + j;
                        guiPanel.updateOriginals();
                    }
                }
            }
            this.scrollBar.yPosition = (int) (((-(this.visibleGuiPanels.get(0).yPosition - 65)) / (this.panelHeight - (double) (this.height - 56))) * (this.height - 56 - this.scrollBar.height) + 56);
            this.horizontalScrollBar.xPosition = (int) ((-this.visibleGuiPanels.get(0).xPosition / (this.panelWidth - (double) this.width)) * (this.width - this.horizontalScrollBar.width));
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean notHoveringOver(int mouseX, int mouseY, int scrollDistance) {
        for (GuiPanel guiPanel : this.visibleGuiPanels) {
            if (guiPanel.scrollBar != null &&
                    mouseX >= guiPanel.xPosition &&
                    mouseX <= guiPanel.xPosition + guiPanel.width &&
                    mouseY >= guiPanel.yPosition &&
                    mouseY <= guiPanel.yPosition + guiPanel.height
            ) {
                guiPanel.updateOriginals();
                guiPanel.scroll(scrollDistance / 3);
                return false;
            }
        }
        return true;
    }

    @Override
    public void initGui() {
        this.searchField = new LifeKnightTextField(0, this.width - 110, 25, 100, 17, "Search") {
            @Override
            public void handleInput() {
                PanelGui.this.loadGuiPanels();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        };
        this.loadGuiPanels();
    }

    protected void loadGuiPanels() {
        this.buttonList.removeIf(guiButton -> guiButton instanceof ScrollBar || guiButton instanceof ScrollBar.HorizontalScrollBar);
        this.visibleGuiPanels.clear();
        this.panelHeight = 0;
        this.panelWidth = 0;

        int nextXPosition = 5;
        int nextYPosition = 65;

        for (GuiPanel guiPanel : this.guiPanels) {
            if (searchField.getText().isEmpty() || guiPanel.name.toLowerCase().contains(this.searchField.getText().toLowerCase())) {
                this.visibleGuiPanels.add(guiPanel);
            }
        }


        if (this.uniformHeight) {
            List<List<GuiPanel>> guiPanelRowList = new ArrayList<>();

            int totalWidth = 0;
            int currentIndex = 0;
            for (GuiPanel guiPanel : this.visibleGuiPanels) {
                guiPanel.resetDimensions();
                if (totalWidth > Video.getGameWidth()) {
                    currentIndex++;
                    guiPanelRowList.add(new ArrayList<>());
                    guiPanelRowList.get(currentIndex).add(guiPanel);
                    totalWidth = guiPanel.width + 10;
                } else {
                    if (guiPanelRowList.size() == currentIndex) {
                        guiPanelRowList.add(new ArrayList<>());
                    }
                    guiPanelRowList.get(currentIndex).add(guiPanel);
                    totalWidth += guiPanel.width + 10;
                }
            }

            for (List<GuiPanel> guiPanelRow : guiPanelRowList) {
                int greatestHeight = 0;
                for (GuiPanel guiPanel : guiPanelRow) {
                    if (guiPanel.height > greatestHeight) greatestHeight = guiPanel.height;
                }
                for (GuiPanel guiPanel : guiPanelRow) {
                    guiPanel.height = greatestHeight;
                }
            }
        }

        for (GuiPanel current : this.visibleGuiPanels) {
            this.buttonList.addAll(current.getGuiButtons());

            if (current.versatile) {
                current.xPosition = nextXPosition;
                current.yPosition = nextYPosition;

                if (current.xPosition + current.width > Video.getGameWidth()) {
                    nextXPosition = 5;
                    nextYPosition = current.yPosition + current.height + 10;
                } else {
                    nextXPosition = current.xPosition + current.width + 10;
                }
                current.updateOriginals();
            }
            this.panelHeight = Math.max(this.panelHeight, current.yPosition + current.height - 45);
            this.panelWidth = Math.max(this.panelWidth, current.xPosition + current.width);
        }

        this.scrollBar = new ScrollBar() {
            @Override
            protected void onMousePress() {
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    guiPanel.updateOriginals();
                }
            }

            @Override
            public void onDrag(int scroll) {
                scroll = -scroll;
                int scaledScroll = (int) (scroll * PanelGui.this.panelHeight / (double) PanelGui.this.height);
                while (scaledScroll > 0 && PanelGui.this.visibleGuiPanels.get(0).originalYPosition + 5 + scaledScroll > 65) {
                    scaledScroll--;
                }
                while (scaledScroll < 0 && this.getLastPanelEndY() + 5 + scaledScroll < PanelGui.this.height) {
                    scaledScroll++;
                }
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    guiPanel.yPosition = guiPanel.originalYPosition + scaledScroll;
                }
            }

            private int getLastPanelEndY() {
                int lastPanelEndY = 0;
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    if (guiPanel.originalYPosition + guiPanel.height > lastPanelEndY) {
                        lastPanelEndY = guiPanel.originalYPosition + guiPanel.height;
                    }
                }
                return lastPanelEndY;
            }
        };
        this.scrollBar.height = (int) ((this.height - 56) * ((this.height - 56) / (double) this.panelHeight));
        this.scrollBar.visible = this.scrollBar.height < this.height - 55 - 5;

        this.buttonList.add(this.scrollBar);

        this.horizontalScrollBar = new ScrollBar.HorizontalScrollBar() {
            @Override
            protected void onMousePress() {
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    guiPanel.updateOriginals();
                }
            }

            @Override
            public void onDrag(int scroll) {
                scroll = -scroll;
                int scaledScroll = (int) (scroll * PanelGui.this.panelWidth / (double) PanelGui.this.width);
                while (scaledScroll > 0 && PanelGui.this.visibleGuiPanels.get(0).originalXPosition + scaledScroll > 5) {
                    scaledScroll--;
                }
                while (scaledScroll < 0 && getRightMostPanelEndX() + 5 + scaledScroll < PanelGui.this.width - 5) {
                    scaledScroll++;
                }
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    guiPanel.xPosition = guiPanel.originalXPosition + scaledScroll;
                }
            }

            protected int getRightMostPanelEndX() {
                int lastPanelEndX = 0;
                for (GuiPanel guiPanel : PanelGui.this.visibleGuiPanels) {
                    if (guiPanel.originalXPosition + guiPanel.width > lastPanelEndX) {
                        lastPanelEndX = guiPanel.originalXPosition + guiPanel.width;
                    }
                }
                return lastPanelEndX;
            }
        };
        this.horizontalScrollBar.width = (int) (this.width * (this.width / (double) this.panelWidth));
        this.horizontalScrollBar.visible = this.horizontalScrollBar.width < this.width - 5;

        this.buttonList.add(this.horizontalScrollBar);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof LifeKnightButton) ((LifeKnightButton) button).work();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.searchField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static class GuiPanel extends Gui {
        boolean versatile = false;
        boolean visible = true;
        private Color color = WHITE;
        private ScrollBar scrollBar = null;
        private ScrollBar.HorizontalScrollBar horizontalScrollBar = null;
        protected final List<GuiButton> guiButtons = new ArrayList<>();
        protected int xPosition;
        protected int yPosition;
        protected int width;
        protected int height;
        protected int originalXPosition;
        protected int originalYPosition;
        protected int originalWidth;
        protected int originalHeight;

        protected int xOffsetPosition = 0;
        protected int yOffsetPosition = 0;
        protected int originalYOffsetPosition = 0;
        protected int originalXOffsetPosition = 0;
        protected String name;

        public GuiPanel(int xPosition, int yPosition, int width, int height, String name) {
            this.xPosition = xPosition;
            this.yPosition = yPosition;
            this.width = width;
            this.height = height;
            this.name = name;
            this.updateOriginals();
        }

        public GuiPanel(int width, int height, String name) {
            this.xPosition = Integer.MIN_VALUE;
            this.yPosition = Integer.MIN_VALUE;
            this.versatile = true;
            this.width = width;
            this.height = height;
            this.name = name;
            this.updateOriginals();
        }

        public void updateOriginals() {
            this.originalXPosition = this.xPosition;
            this.originalYPosition = this.yPosition;
            this.originalWidth = this.width;
            this.originalHeight = this.height;
            this.originalYOffsetPosition = this.yOffsetPosition;
            this.originalXOffsetPosition = this.xOffsetPosition;
        }

        public void resetDimensions() {
            this.updateDimensions();

            if (this.height > 150) {
                this.height = 150;
                int modifiedHeight = this.height - 14;
                if ((int) (modifiedHeight * (modifiedHeight / (double) this.getPanelHeight())) < modifiedHeight - 5) {
                    this.scrollBar = new ScrollBar(-1, this.xPosition + this.width - 5, this.yPosition, 4, getPanelHeight()) {
                        @Override
                        protected void onMousePress() {
                            GuiPanel.this.originalYOffsetPosition = GuiPanel.this.yOffsetPosition;
                        }

                        @Override
                        public void onDrag(int scroll) {
                            scroll = -scroll;
                            int scaledScroll = (int) (scroll * GuiPanel.this.getPanelHeight() / (double) (GuiPanel.this.height - 14));
                            while (scaledScroll > 0 && GuiPanel.this.originalYOffsetPosition + 2 + scaledScroll > 2) {
                                scaledScroll--;
                            }
                            while (scaledScroll < 0 && GuiPanel.this.getLastElementEndY() + 2 + scaledScroll < GuiPanel.this.yPosition + GuiPanel.this.height - 2) {
                                scaledScroll++;
                            }
                            GuiPanel.this.yOffsetPosition = GuiPanel.this.originalYOffsetPosition + scaledScroll;
                        }

                        @Override
                        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                            GlStateManager.pushMatrix();
                            GuiPanel.this.scissor();
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            super.drawButton(mc, mouseX, mouseY);
                            GL11.glDisable(GL11.GL_SCISSOR_TEST);
                            GlStateManager.popMatrix();
                        }
                    };
                    this.scrollBar.height = (int) (modifiedHeight * (modifiedHeight / (double) this.getPanelHeight()));
                    this.scrollBar.visible = true;
                    this.guiButtons.add(this.scrollBar);
                }
            }
            if (this.width > 250) {
                this.width = 250;
                if ((int) (this.width * (this.width / (double) this.getPanelWidth())) < this.width - 5) {
                    this.horizontalScrollBar = new ScrollBar.HorizontalScrollBar(-1, this.xPosition, this.yPosition + this.height - 5, this.width, 4) {
                        @Override
                        protected void onMousePress() {
                            GuiPanel.this.originalXOffsetPosition = GuiPanel.this.xOffsetPosition;
                        }

                        @Override
                        public void onDrag(int scroll) {
                            scroll = -scroll;
                            int scaledScroll = (int) (scroll * GuiPanel.this.getPanelWidth() / (double) GuiPanel.this.width);
                            while (scaledScroll > 0 && GuiPanel.this.originalXOffsetPosition + scaledScroll > 0) {
                                scaledScroll--;
                            }
                            while (scaledScroll < 0 && GuiPanel.this.getLongestElementEndX() + scaledScroll < GuiPanel.this.xPosition + GuiPanel.this.width - 5) {
                                scaledScroll++;
                            }
                            GuiPanel.this.xOffsetPosition = GuiPanel.this.originalXOffsetPosition + scaledScroll;
                        }

                        @Override
                        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                            GlStateManager.pushMatrix();
                            GuiPanel.this.scissor();
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            super.drawButton(mc, mouseX, mouseY);
                            GL11.glDisable(GL11.GL_SCISSOR_TEST);
                            GlStateManager.popMatrix();
                        }
                    };
                    this.horizontalScrollBar.width = (int) (this.width * (this.width / (double) this.getPanelWidth()));
                    this.horizontalScrollBar.visible = true;
                    this.guiButtons.add(this.horizontalScrollBar);
                }
            }
        }

        public void scroll(int distance) {
            while (distance > 0 && this.originalYOffsetPosition + 2 + distance > 2) {
                distance--;
            }
            while (distance < 0 && this.getLastElementEndY() + 2 + distance < GuiPanel.this.yPosition + GuiPanel.this.height - 2) {
                distance++;
            }
            this.yOffsetPosition = this.originalYOffsetPosition + distance;
        }

        protected int getPanelHeight() {
            return this.height;
        }

        protected int getPanelWidth() {
            return this.width;
        }

        protected void updateDimensions() {
            this.width = this.getPanelWidth();
            this.height = this.getPanelHeight() + 14;
        }

        protected int getLastElementEndY() {
            return Math.max(0, this.yPosition + 14 + this.getPanelHeight() - 5 + this.originalYOffsetPosition);
        }

        protected int getLongestElementEndX() {
            return this.xPosition - 5 + this.getPanelWidth() + this.originalXOffsetPosition;
        }

        public List<GuiButton> getGuiButtons() {
            return this.guiButtons;
        }

        public void setColor(Color newColor) {
            this.color = newColor;
        }

        public void drawPanel() {
            if (this.visible && this.yPosition + this.height > 56) {
                if (this.scrollBar != null) {
                    int modifiedHeight = this.height - 14;
                    this.scrollBar.yPosition = (int) (this.yPosition + (modifiedHeight * (-this.yOffsetPosition / (double) this.getPanelHeight()))) + 14;
                    this.scrollBar.xPosition = this.xPosition + this.width - 5;
                }
                if (this.horizontalScrollBar != null) {
                    this.horizontalScrollBar.xPosition = (int) (this.xPosition + (this.width * (-this.xOffsetPosition / (double) this.getPanelWidth())));
                    this.horizontalScrollBar.yPosition = this.yPosition + this.height - 5;
                }
                GlStateManager.pushMatrix();
                int theHeight = this.yPosition < 57 ? this.yPosition + this.height - 57 : this.height;
                Render.glScissor(this.xPosition, Math.max(57, this.yPosition), this.width, theHeight);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                Render.drawRectangle(this.xPosition + 1, this.yPosition + 1, this.xPosition + this.width, this.yPosition + 13, this.color, 170F);
                fontRenderer.drawString(this.name, this.xPosition + this.width / 2 - fontRenderer.getStringWidth(this.name) / 2, this.yPosition + 3, 0xffffffff);
                Render.drawRectangle(this.xPosition, this.yPosition + 14, this.xPosition + this.width + 1, this.yPosition + this.height, Color.BLACK, 62F);
                Render.drawEmptyBox(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 13, Color.BLACK, 150F, 1);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }
        }

        protected void scissor() {
            int theHeight = this.yPosition < 57 ? this.yPosition + this.height - 57 : this.height - 14;
            Render.glScissor(this.xPosition, Math.max(57, this.yPosition + 14), this.width, theHeight);
        }
    }

    public GuiPanel createGuiPanel(int xPosition, int yPosition, int width, int height, String name) {
        GuiPanel guiPanel = new GuiPanel(xPosition, yPosition, width, height, name);
        this.guiPanels.add(guiPanel);
        return guiPanel;
    }

    public GuiPanel createGuiPanel(int width, int height, String name) {
        GuiPanel guiPanel = new GuiPanel(width, height, name);
        this.guiPanels.add(guiPanel);
        return guiPanel;
    }

    protected static class ListPanel extends GuiPanel {
        private final List<?> contents;

        public ListPanel(int xPosition, int yPosition, int width, int height, String name, List<?> contents) {
            super(xPosition, yPosition, width, height, name);
            this.contents = contents;
            this.updateOriginals();
        }

        public ListPanel(String name, List<?> contents) {
            super(0, 0, name);
            this.contents = contents;
            this.updateOriginals();
        }


        @Override
        protected int getPanelHeight() {
            return 12 * this.contents.size() + 5;
        }

        @Override
        protected int getPanelWidth() {
            int longestWidth = 0;
            for (Object object : this.contents) {
                int width;
                if ((width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(object.toString())) > longestWidth) {
                    longestWidth = width;
                }
            }
            return Math.max(Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.name) + 15, longestWidth + 15);
        }

        @Override
        public void drawPanel() {
            super.drawPanel();
            if (this.visible && this.yPosition + this.height > 56) {
                GlStateManager.pushMatrix();
                this.scissor();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                for (int i = 0; i < this.contents.size(); i++) {
                    fontRenderer.drawString(this.contents.get(i).toString(), this.xPosition + 5 + this.xOffsetPosition, this.yPosition + 14 + i * 12 + 2 + this.yOffsetPosition, 0xffffffff);
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }
        }
    }

    public ListPanel createListPanel(String name, List<?> contents) {
        ListPanel listPanel = new ListPanel(name, contents);
        this.guiPanels.add(listPanel);
        return listPanel;
    }

    protected static class ButtonPanel extends GuiPanel {
        private final List<LifeKnightButton.VersatileLifeKnightButton> versatileLifeKnightButtons = new ArrayList<>();

        public ButtonPanel(String name, List<LifeKnightButton.VersatileLifeKnightButton> versatileLifeKnightButtons) {
            super(0, 0, name);

            for (LifeKnightButton.VersatileLifeKnightButton versatileLifeKnightButton : versatileLifeKnightButtons) {
                this.versatileLifeKnightButtons.add(new LifeKnightButton.VersatileLifeKnightButton(versatileLifeKnightButton.displayString, versatileLifeKnightButton.iAction) {
                    @Override
                    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
                        return super.mousePressed(minecraft, mouseX, mouseY) &&
                                mouseX >= ButtonPanel.this.xPosition && mouseX <= ButtonPanel.this.xPosition + ButtonPanel.this.width &&
                                mouseY >= ButtonPanel.this.yPosition && mouseY <= ButtonPanel.this.yPosition + ButtonPanel.this.height;
                    }

                    @Override
                    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
                        if (this.visible) {
                            GlStateManager.pushMatrix();
                            ButtonPanel.this.scissor();
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            int o = ButtonPanel.this.versatileLifeKnightButtons.indexOf(this);
                            this.xPosition = ButtonPanel.this.xPosition + 5 + ButtonPanel.this.xOffsetPosition;
                            this.yPosition = ButtonPanel.this.yPosition + 14 + o * 25 + 2 + ButtonPanel.this.yOffsetPosition;

                            FontRenderer fontRenderer = minecraft.fontRendererObj;
                            minecraft.getTextureManager().bindTexture(buttonTextures);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height &&
                                    mouseX >= ButtonPanel.this.xPosition && mouseX <= ButtonPanel.this.xPosition + ButtonPanel.this.width &&
                                    mouseY >= ButtonPanel.this.yPosition && mouseY <= ButtonPanel.this.yPosition + ButtonPanel.this.height;
                            int i = this.getHoverState(this.hovered);
                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                            GlStateManager.blendFunc(770, 771);
                            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
                            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                            this.mouseDragged(minecraft, mouseX, mouseY);
                            int j = 14737632;

                            if (this.packedFGColour != 0) {
                                j = this.packedFGColour;
                            } else if (!this.enabled) {
                                j = 10526880;
                            } else if (this.hovered) {
                                j = 16777120;
                            }
                            this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
                            GL11.glDisable(GL11.GL_SCISSOR_TEST);
                            GlStateManager.popMatrix();
                        }
                    }
                });
            }
            this.guiButtons.addAll(this.versatileLifeKnightButtons);
        }

        @Override
        protected int getPanelHeight() {
            return 5 + this.versatileLifeKnightButtons.size() * 25 + 5;
        }

        @Override
        protected int getPanelWidth() {
            int longestWidth = 0;
            for (LifeKnightButton.VersatileLifeKnightButton versatileLifeKnightButton : this.versatileLifeKnightButtons) {
                longestWidth = Math.max(longestWidth, versatileLifeKnightButton.width);
            }
            return 5 + longestWidth + 5;
        }
    }

    public ButtonPanel createButtonPanel(String name, List<LifeKnightButton.VersatileLifeKnightButton> versatileLifeKnightButtons) {
        ButtonPanel buttonPanel = new ButtonPanel(name, versatileLifeKnightButtons);
        this.guiPanels.add(buttonPanel);
        return buttonPanel;
    }
}