package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.gui.components.LifeKnightTextField;
import com.lifeknight.combatanalysis.mod.CombatSession;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Render;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import scala.tools.nsc.backend.icode.Members;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

public class CombatSessionFilterGui extends GuiScreen {
    private final GuiScreen lastGui;
    private final List<LifeKnightTextField> lifeKnightTextFields = new ArrayList<>();

    private int resultsFound = 0;

    private boolean deletedSessionsOnly = CombatSession.deletedSessionsOnly;
    private int wonFilterType = CombatSession.wonFilterType;
    private boolean dateFilterType = CombatSession.dateFilterType;
    private final Date firstDate = (Date) CombatSession.firstDate.clone();
    private final Date secondDate = (Date) CombatSession.secondDate.clone();

    private final List<String> opponentFilter = new ArrayList<>(CombatSession.opponentFilter);
    private final List<String> serverFilter = new ArrayList<>(CombatSession.serverFilter);
    private final List<String> typeFilter = new ArrayList<>(CombatSession.typeFilter);

    public CombatSessionFilterGui(GuiScreen lastGui) {
        this.lastGui = lastGui;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.pushMatrix();
        double scale = 5 * (Video.getGameWidth() / (double) Video.getSupposedWidth());
        GlStateManager.scale(scale, scale, scale);
        this.drawString(this.fontRendererObj, "Filter", 1, 2, 0xffffffff);
        GlStateManager.popMatrix();

        this.drawCenteredString(this.fontRendererObj, "Results Found", (2 * this.width / 3) + (this.width / 3) / 2, 70, 0xffffffff);
        this.drawCenteredString(this.fontRendererObj, String.valueOf(this.resultsFound), (2 * this.width / 3) + (this.width / 3) / 2, 55 + ((this.height - 55) / 2), 0xffffffff);

        Render.drawHorizontalLine(0, this.width, 55, new float[]{255, 255, 255}, 255F, 2);

        Render.drawVerticalLine(this.width / 3, 55, this.height, new float[]{255, 255, 255}, 255F, 2);
        Render.drawVerticalLine(2 * this.width / 3, 55, this.height, new float[]{255, 255, 255}, 255F, 2);

        for (LifeKnightTextField lifeKnightTextField : this.lifeKnightTextFields) {
            lifeKnightTextField.drawTextBoxAndName();
            lifeKnightTextField.drawStringBelowBox();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /*
    Reset all button
    */

    @Override
    public void initGui() {
        this.updateResultCount();
        this.lifeKnightTextFields.clear();

        if (this.lastGui != null) {
            this.buttonList.add(new LifeKnightButton("Back", this.buttonList.size(), this.width - 60, 5, 50) {
                @Override
                public void work() {
                    Core.openGui(CombatSessionFilterGui.this.lastGui);
                }
            });
        }
        this.buttonList.add(new LifeKnightButton(this.deletedSessionsOnly ? RED + "Deleted" : GREEN + "Available", this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25, 100) {
            @Override
            public void work() {
                CombatSessionFilterGui.this.deletedSessionsOnly = !CombatSessionFilterGui.this.deletedSessionsOnly;
                this.displayString = CombatSessionFilterGui.this.deletedSessionsOnly ? RED + "Deleted" : GREEN + "Available";
                CombatSessionFilterGui.this.updateResultCount();
            }
        });
        this.buttonList.add(new LifeKnightButton(this.wonFilterType == 0 ? "Won/Lost" : this.wonFilterType == 1 ? GREEN + "Won" : RED + "Lost", this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30, 100) {
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
        this.buttonList.add(new LifeKnightButton("Date - " + (CombatSessionFilterGui.this.dateFilterType ? "Between" : "During"), this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30 * 2, 100) {
            @Override
            public void work() {
                CombatSessionFilterGui.this.dateFilterType = !CombatSessionFilterGui.this.dateFilterType;
                this.displayString = dateFilterType ? "Between" : "During";
                CombatSessionFilterGui.this.lifeKnightTextFields.get(0).setName(CombatSessionFilterGui.this.dateFilterType ? "Between" : "During");
                CombatSessionFilterGui.this.lifeKnightTextFields.get(1).setVisible(CombatSessionFilterGui.this.dateFilterType);
                CombatSessionFilterGui.this.updateResultCount();
            }
        });
        LifeKnightTextField firstDateFilterField;
        this.lifeKnightTextFields.add(firstDateFilterField = new LifeKnightTextField(this.buttonList.size(), (this.width / 3) / 2 - 50, 55 + 25 + 30 * 3 + 10, 100, 17, CombatSessionFilterGui.this.dateFilterType ? "After" : "During") {
            @Override
            public void handleInput() {
                String text = this.getText();
                if (text.isEmpty()) {
                    CombatSessionFilterGui.this.firstDate.setTime(0);
                } else if (text.equalsIgnoreCase("today")) {
                    CombatSessionFilterGui.this.firstDate.setTime(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
                            CombatSessionFilterGui.this.firstDate.setTime(LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
        if (this.firstDate.getTime() != 0) {
            firstDateFilterField.setText(new SimpleDateFormat("MM/dd/yy").format(this.firstDate));
        }

        LifeKnightTextField secondDateFilterField;
        this.lifeKnightTextFields.add(secondDateFilterField = new LifeKnightTextField(this.buttonList.size() + 1, (this.width / 3) / 2 - 50, 55 + 25 + 30 * 4 + 35, 100, 17, "Before") {
            @Override
            public void handleInput() {
                String text = this.getText();
                if (text.isEmpty()) {
                    CombatSessionFilterGui.this.secondDate.setTime(0);
                } else if (text.equalsIgnoreCase("today")) {
                    CombatSessionFilterGui.this.secondDate.setTime(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000L);
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
                            CombatSessionFilterGui.this.secondDate.setTime(LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
        secondDateFilterField.setVisible(this.dateFilterType);
        if (this.secondDate.getTime() != 0) {
            secondDateFilterField.setText(new SimpleDateFormat("MM/dd/yy").format(this.secondDate));
        }

        LifeKnightTextField opponentFilterField;
        this.lifeKnightTextFields.add(opponentFilterField = new LifeKnightTextField(this.buttonList.size() + 2, this.width / 2 - 50, 55 + 25, 100, 17, "Opponents") {
            @Override
            public void handleInput() {
                String text = this.getText();

                CombatSessionFilterGui.this.opponentFilter.clear();
                if (!text.isEmpty() && text.length() > 2) {
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
                if (!text.isEmpty() && text.length() > 2) {
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
                if (!text.isEmpty() && text.length() > 2) {
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
                CombatSession.firstDate = CombatSessionFilterGui.this.firstDate;
                CombatSession.secondDate = CombatSessionFilterGui.this.secondDate;
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
            ((this.dateFilterType && ((this.firstDate.getTime() == 0 || this.secondDate.getTime() == 0) || (combatSession.getStartTime() >= this.firstDate.getTime() && combatSession.getStartTime() <= this.secondDate.getTime() + 86400000L))) ||
                    (!this.dateFilterType && (this.firstDate.getTime() == 0 || (combatSession.getStartTime() >= this.firstDate.getTime() && combatSession.getStartTime() <= this.firstDate.getTime() + 86400000L)))) &&
                    (this.typeFilter.size() == 0 || Text.containsAny(combatSession.detectType(), this.typeFilter, true)) &&
                    (this.serverFilter.size() == 0 || Text.containsAny(combatSession.getServerIp(), this.serverFilter, true)) &&
                    (this.opponentFilter.size() == 0 || Text.containsAny(this.opponentFilter, combatSession.getOpponentNames(), true)
                    )) count++;
        }
        return count;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        ((LifeKnightButton) button).work();
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
