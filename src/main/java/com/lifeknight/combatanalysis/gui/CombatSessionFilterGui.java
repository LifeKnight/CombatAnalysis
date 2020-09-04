package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.gui.components.LifeKnightTextField;
import com.lifeknight.combatanalysis.gui.components.ScrollBar;
import com.lifeknight.combatanalysis.mod.CombatSession;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Render;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.RED;

public class CombatSessionFilterGui extends GuiScreen {
    private final GuiScreen lastGui;
    private final List<LifeKnightTextField> lifeKnightTextFields = new ArrayList<>();
    private ScrollBar scrollBar;

    private int resultsFound = 0;

    private boolean deletedSessionsOnly = CombatSession.deletedSessionsOnly;
    private int wonFilterType = CombatSession.wonFilterType;
    private boolean dateFilterType = CombatSession.dateFilterType;
    private static final Date firstDate = (Date) CombatSession.firstDate.clone();
    private static final Date secondDate = (Date) CombatSession.secondDate.clone();

    private final List<String> opponentFilter = new ArrayList<>(CombatSession.opponentFilter);
    private final List<String> serverFilter = new ArrayList<>(CombatSession.serverFilter);
    private final List<String> typeFilter = new ArrayList<>(CombatSession.typeFilter);

    private LifeKnightButton firstLeftMostPanelButton;
    private LifeKnightButton wonLossFilter;
    private LifeKnightButton dateTypeFilter;

    private LifeKnightTextField firstDateFilterField;
    private LifeKnightTextField secondDateFilterField;

    public CombatSessionFilterGui(GuiScreen lastGui) {
        this.lastGui = lastGui;
        firstDate.setTime(CombatSession.firstDate.getTime());
        secondDate.setTime(CombatSession.secondDate.getTime());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.pushMatrix();
        double scale = 5 * (this.width / (double) Video.getSupposedWidth());
        GlStateManager.scale(scale, scale, scale);
        this.drawString(this.fontRendererObj, "Filter", 1, 2, 0xffffffff);
        GlStateManager.popMatrix();

        this.drawCenteredString(this.fontRendererObj, "Results Found", (2 * this.width / 3) + (this.width / 3) / 2, 70, 0xffffffff);
        this.drawCenteredString(this.fontRendererObj, String.valueOf(this.resultsFound), (2 * this.width / 3) + (this.width / 3) / 2, 55 + ((this.height - 55) / 2), 0xffffffff);

        Render.drawHorizontalLine(55, 0, this.width, Color.WHITE, 255F, 2);

        Render.drawVerticalLine(this.width / 3, 55, this.height, Color.WHITE, 255F, 2);
        Render.drawVerticalLine(2 * this.width / 3, 55, this.height, Color.WHITE, 255F, 2);

        boolean isBetween = CombatSessionFilterGui.this.dateFilterType;
        int leftmostPanelHeight = isBetween ?
                Math.max(this.height - 57, 25 + 30 * 4 + 35 + 30) :
                this.height - 57;
        int j = Mouse.getDWheel() / 13;
        if (mouseX <= this.width / 3 + 2 && mouseY >= 57) {
            if (((j > 0) && this.scrollBar.yPosition > 0) || ((j < 0) && this.scrollBar.yPosition + this.scrollBar.height < this.height)) {
                while (j > 0 && this.firstLeftMostPanelButton.yPosition + j > 55 + 25) {
                    j--;
                }

                LifeKnightTextField lastTextField = isBetween ? this.secondDateFilterField : this.firstDateFilterField;

                while (j < 0 && lastTextField.yPosition + 30 + j < this.height - 10) {
                    j++;
                }

                this.firstLeftMostPanelButton.yPosition += j;
                this.firstLeftMostPanelButton.updateOriginalYPosition();
                this.wonLossFilter.yPosition += j;
                this.wonLossFilter.updateOriginalYPosition();
                this.dateTypeFilter.yPosition += j;
                this.dateTypeFilter.updateOriginalYPosition();

                this.firstDateFilterField.yPosition += j;
                this.firstDateFilterField.updateOriginalYPosition();
                this.secondDateFilterField.yPosition += j;
                this.secondDateFilterField.updateOriginalYPosition();
            }
        }

        int theHeight = this.height - 57;
        this.scrollBar.yPosition = (int) (((-(this.firstLeftMostPanelButton.yPosition - 25 - 55)) / (leftmostPanelHeight - (double) theHeight) * (theHeight - this.scrollBar.height))) + 58;

        for (LifeKnightTextField lifeKnightTextField : this.lifeKnightTextFields) {
            lifeKnightTextField.drawTextBoxAndName();
            lifeKnightTextField.drawStringBelowBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        this.updateResultCount();
        this.buttonList.clear();
        this.lifeKnightTextFields.clear();

        if (this.lastGui != null) {
            this.buttonList.add(new LifeKnightButton("Back", this.buttonList.size(), this.width - 60, 5, 50) {
                @Override
                public void work() {
                    Core.openGui(CombatSessionFilterGui.this.lastGui);
                }
            });
        }
        this.buttonList.add(new LifeKnightButton("Reset", this.buttonList.size(), this.width - 60, 25, 50) {
            @Override
            public void work() {
                CombatSessionFilterGui.this.deletedSessionsOnly = false;
                CombatSessionFilterGui.this.wonFilterType = 0;
                CombatSessionFilterGui.this.dateFilterType = false;
                firstDate.setTime(0);
                secondDate.setTime(0);

                CombatSessionFilterGui.this.opponentFilter.clear();
                CombatSessionFilterGui.this.serverFilter.clear();
                CombatSessionFilterGui.this.typeFilter.clear();

                for (LifeKnightTextField lifeKnightTextField : CombatSessionFilterGui.this.lifeKnightTextFields) {
                    lifeKnightTextField.setText("");
                }
                CombatSessionFilterGui.this.updateResultCount();
            }
        });
        this.buttonList.add(this.firstLeftMostPanelButton = new LifeKnightButton(this.deletedSessionsOnly ? RED + "Deleted" : GREEN + "Available", this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25, 100) {
            @Override
            public void work() {
                CombatSessionFilterGui.this.deletedSessionsOnly = !CombatSessionFilterGui.this.deletedSessionsOnly;
                this.displayString = CombatSessionFilterGui.this.deletedSessionsOnly ? RED + "Deleted" : GREEN + "Available";
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
                GlStateManager.pushMatrix();
                Render.glScissor(this.xPosition, Math.max(this.yPosition, 57), this.width, Math.min(this.height, this.height - (57 - this.yPosition)));
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                if (this.visible) {
                    FontRenderer fontRenderer = minecraft.fontRendererObj;
                    mc.getTextureManager().bindTexture(buttonTextures);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.hovered = mouseX >= this.xPosition && mouseY >= Math.max(this.yPosition, 57) && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                    int hoverState = this.getHoverState(this.hovered);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.blendFunc(770, 771);
                    this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
                    this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);
                    this.mouseDragged(minecraft, mouseX, mouseY);
                    int displayStringColor = 14737632;

                    if (this.packedFGColour != 0) {
                        displayStringColor = this.packedFGColour;
                    } else if (!this.enabled) {
                        displayStringColor = 10526880;
                    } else if (this.hovered) {
                        displayStringColor = 16777120;
                    }

                    this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, displayStringColor);
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }

            @Override
            public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
                return super.mousePressed(minecraft, mouseX, mouseY) && mouseY >= 57;
            }
        });
        this.buttonList.add(this.wonLossFilter = new LifeKnightButton(this.wonFilterType == 0 ? "Won/Lost" : this.wonFilterType == 1 ? GREEN + "Won" : RED + "Lost", this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30, 100) {
            @Override
            public void work() {
                if (CombatSessionFilterGui.this.wonFilterType == 2) {
                    CombatSessionFilterGui.this.wonFilterType = 0;
                } else {
                    CombatSessionFilterGui.this.wonFilterType++;
                }
                this.displayString = CombatSessionFilterGui.this.wonFilterType == 0 ? "Won/Lost" : CombatSessionFilterGui.this.wonFilterType == 1 ? GREEN + "Won" : RED + "Lost";
                CombatSessionFilterGui.this.updateResultCount();
            }
        });
        this.buttonList.add(this.dateTypeFilter = new LifeKnightButton(this.dateFilterType ? "Between" : "During", this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30 * 2, 100) {
            @Override
            public void work() {
                CombatSessionFilterGui.this.dateFilterType = !CombatSessionFilterGui.this.dateFilterType;
                this.displayString = CombatSessionFilterGui.this.dateFilterType ? "Between" : "During";
                CombatSessionFilterGui.this.lifeKnightTextFields.get(0).setName(CombatSessionFilterGui.this.dateFilterType ? "After" : "During");
                CombatSessionFilterGui.this.lifeKnightTextFields.get(1).setVisible(CombatSessionFilterGui.this.dateFilterType);
                boolean isBetween = CombatSessionFilterGui.this.dateFilterType;
                if (!isBetween) {
                    CombatSessionFilterGui.this.initGui();
                    return;
                }
                int panelHeight = Math.max(this.height - 57, 25 + 30 * 4 + 35 + 30);
                int theHeight = CombatSessionFilterGui.this.height - 57;
                CombatSessionFilterGui.this.scrollBar.height = (int) (theHeight * (theHeight / (double) panelHeight));
                CombatSessionFilterGui.this.scrollBar.visible = panelHeight > theHeight;
                CombatSessionFilterGui.this.updateResultCount();
            }
        });
        this.lifeKnightTextFields.add(this.firstDateFilterField = new LifeKnightTextField(this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30 * 3 + 10, 100, 17, CombatSessionFilterGui.this.dateFilterType ? "After" : "During") {
            @Override
            public void handleInput() {
                String text = this.getText();
                if (text.isEmpty()) {
                    firstDate.setTime(0);
                    this.setSubDisplayMessage("");
                } else if (text.equalsIgnoreCase("today")) {
                    firstDate.setTime(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    this.setSubDisplayMessage("");
                } else {
                    try {
                        String monthSection = text.substring(0, text.indexOf("/"));
                        int month = Integer.parseInt(monthSection);
                        String daySection = text.substring(text.indexOf("/") + 1, text.lastIndexOf("/"));
                        int day = Integer.parseInt(daySection);
                        String yearSection = text.substring(text.lastIndexOf("/") + 1);
                        int year = yearSection.length() == 2 ? Integer.parseInt("20" + yearSection) : Integer.parseInt(yearSection);

                        if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1000 || year > 9999) {
                            this.setSubDisplayMessage(RED + "Invalid input!");
                        } else {
                            String dateString = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
                            firstDate.setTime(LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            this.setSubDisplayMessage("");
                        }
                    } catch (Exception exception) {
                        this.setSubDisplayMessage(RED + "Invalid input!");
                    }
                }
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        });

        if (firstDate.getTime() != 0) {
            this.firstDateFilterField.setText(new SimpleDateFormat("MM/dd/yy").format(firstDate));
        }

        this.lifeKnightTextFields.add(this.secondDateFilterField = new LifeKnightTextField(this.buttonList.size() + 1, (this.width / 3) / 2 - 50, 55 + 25 + 30 * 4 + 35, 100, 17, "Before") {
            @Override
            public void handleInput() {
                String text = this.getText();
                if (text.isEmpty()) {
                    secondDate.setTime(0);
                    this.setSubDisplayMessage("");
                } else if (text.equalsIgnoreCase("today")) {
                    secondDate.setTime(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000L);
                    this.setSubDisplayMessage("");
                } else {
                    try {
                        String monthSection = text.substring(0, text.indexOf("/"));
                        int month = Integer.parseInt(monthSection);
                        String daySection = text.substring(text.indexOf("/") + 1, text.lastIndexOf("/"));
                        int day = Integer.parseInt(daySection);
                        String yearSection = text.substring(text.lastIndexOf("/") + 1);
                        int year = yearSection.length() == 2 ? Integer.parseInt("20" + yearSection) : Integer.parseInt(yearSection);

                        if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1000 || year > 9999) {
                            this.setSubDisplayMessage(RED + "Invalid input!");
                        } else {
                            String dateString = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
                            secondDate.setTime(LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            this.setSubDisplayMessage("");
                        }
                    } catch (Exception exception) {
                        this.setSubDisplayMessage(RED + "Invalid input!");
                    }
                }
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        });

        this.secondDateFilterField.setVisible(this.dateFilterType);
        if (secondDate.getTime() != 0) {
            this.secondDateFilterField.setText(new SimpleDateFormat("MM/dd/yy").format(secondDate));
        }

        boolean isBetween = this.dateFilterType;
        int panelHeight = isBetween ?
                Math.max(this.height - 57, 25 + 30 * 4 + 35 + 30) :
                this.height - 57;

        this.buttonList.add(this.scrollBar = new ScrollBar(this.buttonList.size(), this.width / 3 - 6, 58, 4, 0) {
            @Override
            public void onDrag(int scroll) {
                scroll = -scroll;

                int scaledScroll = (int) (scroll * panelHeight / (double) CombatSessionFilterGui.this.height);
                LifeKnightTextField lastTextField = CombatSessionFilterGui.this.dateFilterType ?
                        CombatSessionFilterGui.this.secondDateFilterField : CombatSessionFilterGui.this.firstDateFilterField;

                while (scaledScroll > 0 && CombatSessionFilterGui.this.firstLeftMostPanelButton.originalYPosition + scaledScroll > 55 + 25) {
                    scaledScroll--;
                }

                while (scaledScroll < 0 && lastTextField.originalYPosition + 30 + scaledScroll < CombatSessionFilterGui.this.height - 10) {
                    scaledScroll++;
                }

                CombatSessionFilterGui.this.firstLeftMostPanelButton.yPosition = CombatSessionFilterGui.this.firstLeftMostPanelButton.originalYPosition + scaledScroll;
                CombatSessionFilterGui.this.wonLossFilter.yPosition = CombatSessionFilterGui.this.wonLossFilter.originalYPosition + scaledScroll;
                CombatSessionFilterGui.this.dateTypeFilter.yPosition = CombatSessionFilterGui.this.dateTypeFilter.originalYPosition + scaledScroll;

                CombatSessionFilterGui.this.firstDateFilterField.yPosition = CombatSessionFilterGui.this.firstDateFilterField.originalYPosition + scaledScroll;
                CombatSessionFilterGui.this.secondDateFilterField.yPosition = CombatSessionFilterGui.this.secondDateFilterField.originalYPosition + scaledScroll;
            }

            @Override
            public void onMousePress() {
                CombatSessionFilterGui.this.firstLeftMostPanelButton.updateOriginalYPosition();
                CombatSessionFilterGui.this.wonLossFilter.updateOriginalYPosition();
                CombatSessionFilterGui.this.dateTypeFilter.updateOriginalYPosition();
                CombatSessionFilterGui.this.firstDateFilterField.updateOriginalYPosition();
                CombatSessionFilterGui.this.secondDateFilterField.updateOriginalYPosition();
            }
        });

        int theHeight = this.height - 57;

        this.scrollBar.height = (int) (theHeight * (theHeight / (double) panelHeight));
        this.scrollBar.visible = panelHeight > theHeight && isBetween;

        LifeKnightTextField opponentFilterField;
        this.lifeKnightTextFields.add(opponentFilterField = new LifeKnightTextField(this.buttonList.size() + 2, this.width / 2 - 50, 55 + 25, 100, 17, "Opponents") {
            @Override
            public void handleInput() {
                String text = this.getText();

                CombatSessionFilterGui.this.opponentFilter.clear();
                if (!text.isEmpty()) {
                    if (text.contains(",")) {
                        String[] opponents = text.split(",");
                        CombatSessionFilterGui.this.opponentFilter.addAll(Arrays.asList(opponents));
                    } else {
                        CombatSessionFilterGui.this.opponentFilter.add(text);
                    }
                }
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        });

        opponentFilterField.setText(Miscellaneous.toCSV(this.opponentFilter));

        LifeKnightTextField serverFilterField;
        this.lifeKnightTextFields.add(serverFilterField = new LifeKnightTextField(this.buttonList.size() + 3, this.width / 2 - 50, 55 + 25 + 55, 100, 17, "Servers") {
            @Override
            public void handleInput() {
                String text = this.getText();

                CombatSessionFilterGui.this.serverFilter.clear();
                if (!text.isEmpty()) {
                    if (text.contains(",")) {
                        String[] servers = text.split(",");
                        CombatSessionFilterGui.this.serverFilter.addAll(Arrays.asList(servers));
                    } else {
                        CombatSessionFilterGui.this.serverFilter.add(text);
                    }
                }
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        });

        serverFilterField.setText(Miscellaneous.toCSV(this.serverFilter));

        LifeKnightTextField typeFilterField;
        this.lifeKnightTextFields.add(typeFilterField = new LifeKnightTextField(this.buttonList.size() + 4, this.width / 2 - 50, 55 + 25 + 55 * 2, 100, 17, "Types") {
            @Override
            public void handleInput() {
                String text = this.getText();

                CombatSessionFilterGui.this.typeFilter.clear();
                if (!text.isEmpty()) {
                    if (text.contains(",")) {
                        String[] opponents = text.split(",");
                        CombatSessionFilterGui.this.typeFilter.addAll(Arrays.asList(opponents));
                    } else {
                        CombatSessionFilterGui.this.typeFilter.add(text);
                    }
                }
                CombatSessionFilterGui.this.updateResultCount();
            }

            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                }
                return false;
            }
        });

        typeFilterField.setText(Miscellaneous.toCSV(this.typeFilter));

        this.buttonList.add(new LifeKnightButton("Go", this.buttonList.size() + 5, (2 * this.width / 3) + (this.width / 3) / 2 - 50, this.height - 30, 100) {
            @Override
            public void work() {
                CombatSession.deletedSessionsOnly = CombatSessionFilterGui.this.deletedSessionsOnly;
                CombatSession.wonFilterType = CombatSessionFilterGui.this.wonFilterType;
                CombatSession.dateFilterType = CombatSessionFilterGui.this.dateFilterType;
                CombatSession.firstDate.setTime(firstDate.getTime());
                CombatSession.secondDate.setTime(secondDate.getTime());
                CombatSession.opponentFilter = CombatSessionFilterGui.this.opponentFilter;
                CombatSession.serverFilter = CombatSessionFilterGui.this.serverFilter;
                CombatSession.typeFilter = CombatSessionFilterGui.this.typeFilter;
                Core.openGui(new CombatSessionGui(CombatSession.getLatestAnalysisForGui()));
            }
        });
    }

    private void updateResultCount() {
        this.resultsFound = this.getResultsFound();
    }

    private int getResultsFound() {
        int count = 0;
        for (CombatSession combatSession : CombatSession.getCombatSessions()) {
            if ((this.deletedSessionsOnly && combatSession.isDeleted()) || (!this.deletedSessionsOnly && !combatSession.isDeleted()) &&
                    (this.wonFilterType == 0 || ((this.wonFilterType == 1 && combatSession.isWon()) || (this.wonFilterType == 2 && !combatSession.isWon()))) &&
                    ((this.dateFilterType && ((firstDate.getTime() == 0 || secondDate.getTime() == 0) || (combatSession.getStartTime() >= firstDate.getTime() && combatSession.getStartTime() <= secondDate.getTime() + 86400000L))) ||
                            (!this.dateFilterType && (firstDate.getTime() == 0 || (combatSession.getStartTime() >= firstDate.getTime() && combatSession.getStartTime() <= firstDate.getTime() + 86400000L)))) &&
                    (this.typeFilter.isEmpty() || Text.containsAny(combatSession.detectType(), this.typeFilter, true, true)) &&
                    (this.serverFilter.isEmpty() || (Text.containsAny(combatSession.getServerIp(), this.serverFilter, true, true) || Text.containsAny(combatSession.getScoreboardDisplayName(), this.serverFilter, true, true))) &&
                    (this.opponentFilter.isEmpty() || Text.containsAny(this.opponentFilter, combatSession.getOpponentNames(), true, true)
                    )) count++;
        }
        return count;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof LifeKnightButton) ((LifeKnightButton) button).work();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (LifeKnightTextField lifeKnightTextField : this.lifeKnightTextFields) {
            lifeKnightTextField.textboxKeyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (LifeKnightTextField lifeKnightTextField : this.lifeKnightTextFields) {
            lifeKnightTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
}
