package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.*;
import com.lifeknight.combatanalysis.utilities.Video;
import com.lifeknight.combatanalysis.variables.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.MOD_COLOR;
import static com.lifeknight.combatanalysis.mod.Core.openGui;
import static net.minecraft.util.EnumChatFormatting.*;

public class LifeKnightGui extends GuiScreen {
    private final String name;
    private final List<LifeKnightVariable> guiVariables = new ArrayList<>();
    private final List<LifeKnightTextField> textFields = new ArrayList<>();
    private int panelHeight = 0;
    private final List<GuiButton> displayedButtons = new ArrayList<>();
    private final List<LifeKnightButton> extraButtons = new ArrayList<>();
    private ScrollBar scrollBar;
    private LifeKnightTextField searchField;
    private String panelMessage = "";
    private final List<String> groupNames = new ArrayList<>(Collections.singletonList("All"));
    public String selectedGroup = "All";

    public LifeKnightGui(String name, List<LifeKnightVariable> lifeKnightVariables) {
        this.name = name;
        for (LifeKnightVariable lifeKnightVariable : lifeKnightVariables) {
            if (lifeKnightVariable.showInLifeKnightGui()) {
                this.guiVariables.add(lifeKnightVariable);
                if (!this.groupNames.contains(lifeKnightVariable.getGroup())) {
                    this.groupNames.add(lifeKnightVariable.getGroup());
                }
            }
        }
    }

    public LifeKnightGui(String name, LifeKnightVariable... lifeKnightVariables) {
        this(name, Arrays.asList(lifeKnightVariables));
    }

    public LifeKnightGui(String name, List<LifeKnightVariable> lifeKnightVariables, List<LifeKnightButton> extraButtons) {
        this(name, lifeKnightVariables);
        this.extraButtons.addAll(extraButtons);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        super.drawCenteredString(this.fontRendererObj, MOD_COLOR + this.name, Video.getScaledWidth(150), Video.getScaledHeight(60), 0xffffffff);
        super.drawCenteredString(this.fontRendererObj, this.panelMessage, Video.get2ndPanelCenter(), this.height / 2, 0xffffffff);
        super.drawVerticalLine(Video.getScaledWidth(300), 0, this.height, 0xffffffff);
        this.searchField.drawTextBoxAndName();

        for (int i = 0; i < this.groupNames.size() - 1; i++) {
            drawHorizontalLine(Video.getScaledWidth(100), Video.getScaledWidth(200), Video.getScaledHeight(150) + 25 * i + 22, 0xffffffff);
        }

        if (this.displayedButtons.size() != 0) {
            int j = Mouse.getDWheel() / 7;
            if (((j > 0) && this.scrollBar.yPosition > 0) || ((j < 0) && this.scrollBar.yPosition + this.scrollBar.height < this.height)) {
                while (j > 0 && this.displayedButtons.get(0).yPosition + j > 10) {
                    j--;
                }

                while (j < 0 && this.displayedButtons.get(this.displayedButtons.size() - 1).yPosition + 30 + j < this.height - 10) {
                    j++;
                }

                for (GuiButton guiButton : this.displayedButtons) {
                    guiButton.yPosition += j;
                    if (guiButton instanceof LifeKnightButton) {
                        ((LifeKnightButton) guiButton).updateOriginalYPosition();
                    } else if (guiButton instanceof LifeKnightSlider) {
                        ((LifeKnightSlider) guiButton).updateOriginalYPosition();
                    }
                }
                for (GuiButton guiButton : LifeKnightGui.super.buttonList) {
                    if (guiButton instanceof LifeKnightButton && (guiButton.displayString.equals(">") || guiButton.displayString.equals("<"))) {
                        guiButton.yPosition += j;
                        ((LifeKnightButton) guiButton).updateOriginalYPosition();
                    }
                }
                for (LifeKnightTextField lifeKnightTextField : this.textFields) {
                    lifeKnightTextField.yPosition += j;
                    lifeKnightTextField.updateOriginalYPosition();
                }
            }
            this.scrollBar.yPosition = (int) (((-(displayedButtons.get(0).yPosition - 10)) / (this.panelHeight - (double) this.height) * (this.height - this.scrollBar.height)));
        } else {
            this.scrollBar.visible = false;
        }

        for (LifeKnightTextField lifeKnightTextField : textFields) {
            if (((this.selectedGroup.equals("All") || this.selectedGroup.equals(lifeKnightTextField.lifeKnightVariable.getGroup())) && (this.searchField.getText().isEmpty() || lifeKnightTextField.lifeKnightVariable.getName().toLowerCase().contains(this.searchField.getText().toLowerCase())))) {
                lifeKnightTextField.drawTextBoxAndName();
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void initGui() {
        this.searchField = new LifeKnightTextField(0, Video.getScaledWidth(75), this.height - 40, Video.getScaledWidth(150), 20, "Search") {
            @Override
            public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
                if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
                    this.handleInput();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void handleInput() {
                listComponents();
            }
        };
        this.listComponents();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof LifeKnightButton) {
            ((LifeKnightButton) button).work();
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != 1) {
            this.searchField.textboxKeyTyped(typedChar, keyCode);
            for (LifeKnightTextField lifeKnightTextField : this.textFields) {
                lifeKnightTextField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        for (LifeKnightTextField lifeKnightTextField : this.textFields) {
            lifeKnightTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void listComponents() {
        this.textFields.clear();
        super.buttonList.clear();
        this.displayedButtons.clear();
        this.panelHeight = 5;
        boolean noButtons = true;
        int componentId = 0;

        for (LifeKnightVariable lifeKnightVariable : this.guiVariables) {
            if (((this.selectedGroup.equals("All") || this.selectedGroup.equals(lifeKnightVariable.getGroup())) && (this.searchField.getText().isEmpty() || lifeKnightVariable.getName().toLowerCase().contains(this.searchField.getText().toLowerCase()) || lifeKnightVariable.getCustomDisplayString().toLowerCase().contains(this.searchField.getText().toLowerCase())))) {
                noButtons = false;
                if (lifeKnightVariable instanceof LifeKnightBoolean) {
                    if (((LifeKnightBoolean) lifeKnightVariable).hasList()) {
                        LifeKnightButton open;
                        super.buttonList.add(open = new LifeKnightButton(componentId,
                                Video.get2ndPanelCenter() + 110,
                                10 + componentId * 30,
                                20,
                                20, ">") {
                            @Override
                            public void work() {
                                if (((LifeKnightBoolean) lifeKnightVariable).getList() instanceof LifeKnightList<?>) {
                                    openGui(new ListGui((LifeKnightList<?>) ((LifeKnightBoolean) lifeKnightVariable).getList(), LifeKnightGui.this));
                                } else {
                                    openGui(new LifeKnightObjectListGui((LifeKnightObjectList) ((LifeKnightBoolean) lifeKnightVariable).getList(), LifeKnightGui.this));
                                }
                            }
                        });
                        this.displayedButtons.add(new LifeKnightBooleanButton(componentId, (LifeKnightBoolean) lifeKnightVariable, open));
                    } else {
                        this.displayedButtons.add(new LifeKnightBooleanButton(componentId, (LifeKnightBoolean) lifeKnightVariable, null));
                    }
                    this.panelHeight += 30;
                    componentId++;
                } else if (lifeKnightVariable instanceof LifeKnightNumber) {
                    if (!(lifeKnightVariable instanceof LifeKnightNumber.LifeKnightLong)) {
                        displayedButtons.add(new LifeKnightSlider(componentId, false, (LifeKnightNumber) lifeKnightVariable));
                        this.panelHeight += 30;
                        componentId++;
                    } else {
                        int i = this.textFields.size();
                        this.textFields.add(new LifeKnightTextField(componentId + 1, Video.get2ndPanelCenter() - 100,
                                (componentId + 1) * 30 + 10,
                                200,
                                20,
                                lifeKnightVariable.getCustomDisplayString()) {
                            @Override
                            public void handleInput() {
                                if (!this.getText().isEmpty()) {
                                    try {
                                        this.lastInput = this.getText();
                                        this.setText("");
                                        long l = Long.parseLong(this.lastInput);
                                        if (l >= (Long) ((LifeKnightNumber.LifeKnightLong) this.lifeKnightVariable).getMinimumValue() && l <= (Long) ((LifeKnightNumber.LifeKnightLong) lifeKnightVariable).getMaximumValue()) {
                                            ((LifeKnightNumber.LifeKnightLong) this.lifeKnightVariable).setValue(l);
                                        } else {
                                            throw new Exception();
                                        }
                                        this.name = this.lifeKnightVariable.getCustomDisplayString();
                                    } catch (Exception e) {
                                        this.name = RED + "Invalid input!";
                                    }
                                }
                            }
                        });
                        super.buttonList.add(new LifeKnightButton(componentId + 1, Video.get2ndPanelCenter() + 110,
                                10 + (componentId + 1) * 30,
                                20,
                                20, ">") {
                            @Override
                            public void work() {
                                textFields.get(i).handleInput();
                            }
                        });
                        this.panelHeight += 60;
                        componentId += 2;
                    }
                } else if (lifeKnightVariable instanceof LifeKnightString) {
                    int i = this.textFields.size();
                    this.textFields.add(new LifeKnightTextField(componentId + 1, lifeKnightVariable) {
                        @Override
                        public void handleInput() {
                            if (!this.getText().isEmpty()) {
                                this.lastInput = this.getText();
                                this.setText("");
                                ((LifeKnightString) this.lifeKnightVariable).setValue(this.lastInput);
                                this.name = this.lifeKnightVariable.getCustomDisplayString();
                            }
                        }
                    });
                    super.buttonList.add(new LifeKnightButton(componentId, Video.get2ndPanelCenter() + 110,
                            10 + (componentId + 1) * 30,
                            20,
                            20, ">") {
                        @Override
                        public void work() {
                            textFields.get(i).handleInput();
                        }
                    });
                    this.panelHeight += 60;
                    componentId += 2;
                } else if (lifeKnightVariable instanceof LifeKnightCycle) {
                    LifeKnightButton previous;
                    super.buttonList.add(previous = new LifeKnightButton(componentId,
                            Video.get2ndPanelCenter() - 130,
                            10 + componentId * 30,
                            20,
                            20, "<") {
                        @Override
                        public void work() {
                            ((LifeKnightCycle) lifeKnightVariable).previous();
                        }
                    });
                    LifeKnightButton next;
                    super.buttonList.add(next = new LifeKnightButton(componentId,
                            Video.get2ndPanelCenter() + 110,
                            10 + componentId * 30,
                            20,
                            20, ">") {
                        @Override
                        public void work() {
                            ((LifeKnightCycle) lifeKnightVariable).next();
                        }
                    });
                    this.displayedButtons.add(new LifeKnightButton(componentId, lifeKnightVariable.getName() + ": " + YELLOW + ((LifeKnightCycle) lifeKnightVariable).getCurrentValueString()) {
                        @Override
                        public void work() {
                            ((LifeKnightCycle) lifeKnightVariable).next();
                        }

                        @Override
                        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                            this.displayString = lifeKnightVariable.getCustomDisplayString();
                            this.width = Math.max(200, Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 20);
                            this.xPosition = Video.get2ndPanelCenter() - this.width / 2;
                            previous.xPosition = this.xPosition - 30;
                            next.xPosition = this.xPosition + this.width + 10;
                            super.drawButton(mc, mouseX, mouseY);
                        }
                    });
                    this.panelHeight += 30;
                    componentId++;
                } else if (lifeKnightVariable instanceof LifeKnightList && ((LifeKnightList<?>) lifeKnightVariable).isIndependent()) {
                    this.displayedButtons.add(new LifeKnightButton(componentId, lifeKnightVariable.getCustomDisplayString()) {
                        @Override
                        public void work() {
                            openGui(new ListGui((LifeKnightList<?>) lifeKnightVariable, LifeKnightGui.this));
                        }
                    });
                    this.panelHeight += 30;
                    componentId++;
                } else if (lifeKnightVariable instanceof LifeKnightObject) {
                    this.displayedButtons.add(new LifeKnightButton(componentId, lifeKnightVariable.getCustomDisplayString()) {
                        @Override
                        public void work() {
                            openGui(new LifeKnightObjectGui((LifeKnightObject) lifeKnightVariable, LifeKnightGui.this));
                        }
                    });
                    this.panelHeight += 30;
                    componentId++;
                } else if (lifeKnightVariable instanceof LifeKnightObjectList && ((LifeKnightObjectList) lifeKnightVariable).isIndependent()) {
                    this.displayedButtons.add(new LifeKnightButton(componentId, lifeKnightVariable.getCustomDisplayString()) {
                        @Override
                        public void work() {
                            openGui(new LifeKnightObjectListGui((LifeKnightObjectList) lifeKnightVariable, LifeKnightGui.this));
                        }
                    });
                    this.panelHeight += 30;
                    componentId++;
                }
            }
        }

        for (LifeKnightButton lifeKnightButton : extraButtons) {
            if ((this.selectedGroup.equals("All") && (this.searchField.getText().isEmpty() || lifeKnightButton.displayString.toLowerCase().contains(this.searchField.getText().toLowerCase())))) {
                noButtons = false;
                lifeKnightButton.xPosition = Video.get2ndPanelCenter() - 100;
                lifeKnightButton.yPosition = componentId * 30 + 10;
                lifeKnightButton.id = componentId;

                this.displayedButtons.add(lifeKnightButton);

                this.panelHeight += 30;
                componentId++;
            }
        }
        super.buttonList.addAll(displayedButtons);

        for (int i = 0; i < this.groupNames.size(); i++) {
            int finalI = i;
            super.buttonList.add(new LifeKnightButton(super.buttonList.size() - 1, Video.getScaledWidth(100), Video.getScaledHeight(150) + 25 * i, Video.getScaledWidth(100), 20, this.groupNames.get(i)) {
                final String name = LifeKnightGui.this.groupNames.get(finalI);

                @Override
                public void work() {
                    LifeKnightGui.this.selectedGroup = this.name;
                    LifeKnightGui.this.listComponents();
                }

                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    if (this.visible) {
                        FontRenderer fontrenderer = mc.fontRendererObj;
                        this.displayString = (LifeKnightGui.this.selectedGroup.equals(this.name) ? MOD_COLOR + "" + BOLD : "") + this.name;
                        this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffffffff);
                    }
                }
            });
        }
        this.panelMessage = noButtons ? GRAY + "No settings found" : "";
        super.buttonList.add(this.scrollBar = new ScrollBar() {
            @Override
            public void onDrag(int scroll) {
                scroll = -scroll;

                int scaledScroll = (int) (scroll * LifeKnightGui.this.panelHeight / (double) LifeKnightGui.this.height);
                Object lastComponent = null;
                int highestComponentId;

                if (LifeKnightGui.this.displayedButtons.size() == 0) {
                    highestComponentId = Math.max(0, LifeKnightGui.this.textFields.size() - 1);
                } else if (LifeKnightGui.this.textFields.size() == 0) {
                    highestComponentId = Math.max(0, LifeKnightGui.this.displayedButtons.size() - 1);
                } else {
                    highestComponentId = LifeKnightGui.this.displayedButtons.size() + LifeKnightGui.this.textFields.size() - 2;
                }

                for (GuiButton guiButton : LifeKnightGui.this.displayedButtons) {
                    if (guiButton.id == highestComponentId) {
                        lastComponent = guiButton;
                        break;
                    }
                }

                if (lastComponent != null) {
                    for (LifeKnightTextField lifeKnightTextField : LifeKnightGui.this.textFields) {
                        if (lifeKnightTextField.getId() == highestComponentId) {
                            lastComponent = lifeKnightTextField;
                            break;
                        }
                    }
                }

                while (scaledScroll > 0 && LifeKnightGui.this.getFirstComponentOriginalYPosition() + scaledScroll > 10) {
                    scaledScroll--;
                }

                if (lastComponent instanceof LifeKnightButton) {
                    while (scaledScroll < 0 && ((LifeKnightButton) lastComponent).originalYPosition + 30 + scaledScroll < LifeKnightGui.this.height - 10) {
                        scaledScroll++;
                    }
                } else if (lastComponent instanceof LifeKnightSlider) {
                    while (scaledScroll < 0 && ((LifeKnightSlider) lastComponent).originalYPosition + 30 + scaledScroll < LifeKnightGui.this.height - 10) {
                        scaledScroll++;
                    }
                } else if (lastComponent instanceof LifeKnightTextField) {
                    while (scaledScroll < 0 && ((LifeKnightTextField) lastComponent).originalYPosition + 30 + scaledScroll < LifeKnightGui.this.height - 10) {
                        scaledScroll++;
                    }
                }

                for (GuiButton guiButton : LifeKnightGui.this.displayedButtons) {
                    if (guiButton instanceof LifeKnightButton) {
                        guiButton.yPosition = ((LifeKnightButton) guiButton).originalYPosition + scaledScroll;
                    } else if (guiButton instanceof LifeKnightSlider) {
                        guiButton.yPosition = ((LifeKnightSlider) guiButton).originalYPosition + scaledScroll;
                    }
                }

                for (GuiButton guiButton : LifeKnightGui.super.buttonList) {
                    if (guiButton instanceof LifeKnightButton && guiButton.displayString.equals(">") || guiButton.displayString.equals("<")) {
                        guiButton.yPosition = ((LifeKnightButton) guiButton).originalYPosition + scaledScroll;
                    }
                }
                for (LifeKnightTextField lifeKnightTextField : textFields) {
                    lifeKnightTextField.yPosition = lifeKnightTextField.originalYPosition + scaledScroll;
                }
            }

            @Override
            public void onMousePress() {
                for (GuiButton guiButton : LifeKnightGui.this.displayedButtons) {
                    if (guiButton instanceof LifeKnightButton) {
                        ((LifeKnightButton) guiButton).updateOriginalYPosition();
                    } else if (guiButton instanceof LifeKnightSlider) {
                        ((LifeKnightSlider) guiButton).updateOriginalYPosition();
                    }
                }
                for (GuiButton guiButton : LifeKnightGui.super.buttonList) {
                    if (guiButton instanceof LifeKnightButton && guiButton.displayString.equals(">")) {
                        ((LifeKnightButton) guiButton).updateOriginalYPosition();
                    }
                }
                for (LifeKnightTextField lifeKnightTextField : LifeKnightGui.this.textFields) {
                    lifeKnightTextField.updateOriginalYPosition();
                }
            }
        });

        this.scrollBar.height = (int) (this.height * (this.height / (double) this.panelHeight));
        this.scrollBar.visible = this.scrollBar.height < this.height;
    }

    private int getFirstComponentOriginalYPosition() {
        Object firstComponent = this.getFirstComponent();

        if (firstComponent instanceof LifeKnightButton) {
            return ((LifeKnightButton) firstComponent).originalYPosition;
        } else if (firstComponent instanceof LifeKnightSlider) {
            return ((LifeKnightSlider) firstComponent).originalYPosition;
        } else if (firstComponent instanceof LifeKnightTextField) {
            return ((LifeKnightTextField) firstComponent).originalYPosition;
        }
        return 0;
    }

    private int getFirstComponentYPosition() {
        Object firstComponent = this.getFirstComponent();

        if (firstComponent instanceof GuiButton) {
            return ((GuiButton) firstComponent).yPosition;
        } else if (firstComponent instanceof LifeKnightTextField) {
            return ((LifeKnightTextField) firstComponent).yPosition;
        }
        return 0;
    }

    private Object getFirstComponent() {
        for (GuiButton guiButton : this.displayedButtons) {
            if (guiButton.id == 0) {
                return guiButton;
            }
        }
        for (LifeKnightTextField lifeKnightTextField : this.textFields) {
            if (lifeKnightTextField.getId() == 0) {
                return lifeKnightTextField;
            }
        }
        return 0;
    }
}