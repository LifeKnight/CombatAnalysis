package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.*;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.variables.LifeKnightStringList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

import static com.lifeknight.combatanalysis.utilities.Miscellaneous.*;
import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.RED;

public class ListGui extends GuiScreen {
    private final ArrayList<ListItemButton> listItemButtons = new ArrayList<>();
    private final ArrayList<LifeKnightButton> lifeKnightButtons = new ArrayList<>();
    private final LifeKnightStringList lifeKnightStringList;
    private ConfirmButton clearButton;
    private ScrollBar scrollBar;
    private LifeKnightTextField addField, searchField;
    public ListItemButton selectedItem;
    public LifeKnightButton addButton, removeButton;
    private String textFieldSubMessage = "", searchInput = "", listMessage = "";
    public LifeKnightGui lastGui;

    public ListGui(LifeKnightStringList lifeKnightStringList) {
        this.lifeKnightStringList = lifeKnightStringList;
        lastGui = null;
    }

    public ListGui(LifeKnightStringList lifeKnightStringList, LifeKnightGui lifeKnightGui) {
        this(lifeKnightStringList);
        lastGui = lifeKnightGui;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(fontRendererObj, listMessage, get2ndPanelCenter(), super.height / 2, 0xffffffff);
        drawVerticalLine(getScaledWidth(300), 0, super.height, 0xffffffff);
        searchField.drawTextBoxAndName();
        addField.drawTextBoxAndName();
        addField.drawStringBelowBox();

        if (listItemButtons.size() != 0) {
            int panelHeight = listItemButtons.size() * 30;

            scrollBar.height = (int) (super.height * (super.height / (double) panelHeight));
            int j = Mouse.getDWheel() / 7;
            scrollBar.visible = !(scrollBar.height >= super.height);
                while (j > 0 && listItemButtons.get(0).yPosition + j > 10) {
                    j--;
                }

                while (j < 0 && listItemButtons.get(listItemButtons.size() - 1).yPosition + 30 + j < super.height - 10) {
                    j++;
                }
                for (ListItemButton listItemButton : listItemButtons) {
                    listItemButton.yPosition += j;
                }
            scrollBar.yPosition = (int) ((super.height * (-listItemButtons.get(0).yPosition - 10) / (double) (panelHeight - super.height)) * ((super.height - scrollBar.height) / (double) super.height)) + 8;
        } else {
            scrollBar.visible = false;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void initGui() {
        searchField = new LifeKnightTextField(0, getScaledWidth(75), this.height - 40, getScaledWidth(150), 20, "Search") {
            @Override
            public String getSubDisplayString() {
                return null;
            }

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
                searchInput = this.getText();
                listItems();
            }
        };

        addField = new LifeKnightTextField(1, getScaledWidth(75), getScaledHeight(85), getScaledWidth(150), 20, lifeKnightStringList.getName()) {
            @Override
            public void handleInput() {
                this.lastInput = this.getText();
                this.setText("");
                try {
                    if (!lastInput.isEmpty()) {
                        lifeKnightStringList.addElement(lastInput);
                        textFieldSubMessage = "";
                        listItems();
                    }
                } catch (IOException ioException) {
                    textFieldSubMessage = RED + ioException.getMessage();
                }
            }

            @Override
            public String getSubDisplayString() {
                return textFieldSubMessage;
            }
        };

        super.buttonList.add(addButton = new LifeKnightButton("Add", 2, getScaledWidth(75), getScaledHeight(165), getScaledWidth(150)) {
            @Override
            public void work() {
                addField.handleInput();
                selectedItem = null;
            }
        });

        super.buttonList.add(removeButton = new LifeKnightButton("Remove", 3, getScaledWidth(75), getScaledHeight(230), getScaledWidth(150)) {
            @Override
            public void work() {
                removeSelectedButton();
            }
        });
        removeButton.visible = false;

        super.buttonList.add(clearButton = new ConfirmButton(4, getScaledWidth(75), getScaledHeight(295), getScaledWidth(150), "Clear", RED + "Confirm") {
            @Override
            public void onConfirm() {
                lifeKnightStringList.clear();
                listItems();
            }
        });
        clearButton.visible = false;

        super.buttonList.add(scrollBar = new ScrollBar() {
            @Override
            public void onDrag(int scroll) {
                scroll = -scroll;
                int scaledScroll = (int) (scroll * (listItemButtons.size() * 30) / (double) ListGui.super.height);
                while (scaledScroll > 0 && listItemButtons.get(0).originalYPosition + scaledScroll > 10) {
                    scaledScroll--;
                }
                while (scaledScroll < 0 && listItemButtons.get(listItemButtons.size() - 1).originalYPosition + 30 + scaledScroll < ListGui.super.height - 10) {
                    scaledScroll++;
                }
                for (ListItemButton listItemButton : listItemButtons) {
                    listItemButton.yPosition = listItemButton.originalYPosition + scaledScroll;
                }
            }

            @Override
            public void onMousePress() {
                for (ListItemButton listItemButton : listItemButtons) {
                    listItemButton.updateOriginalYPosition();
                }
            }
        });

        lifeKnightButtons.add(addButton);
        lifeKnightButtons.add(removeButton);
        lifeKnightButtons.add(clearButton);
        if (lastGui != null) {
            LifeKnightButton backButton;
            super.buttonList.add(backButton = new LifeKnightButton("Back", 5, getScaledHeight(10), getScaledHeight(10), 50) {
                @Override
                public void work() {
                    Core.openGui(lastGui);
                }
            });
            lifeKnightButtons.add(backButton);
        }
        listItems();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ListItemButton) {
            ((ListItemButton) button).work();
        } else if (button instanceof LifeKnightButton) {
            ((LifeKnightButton) button).work();
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 0xD3 && selectedItem != null) {
            removeSelectedButton();
        } else {
            addField.textboxKeyTyped(typedChar, keyCode);
            searchField.textboxKeyTyped(typedChar, keyCode);
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        addField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void removeSelectedButton() {
        try {
            lifeKnightStringList.removeElement(selectedItem.getButtonText());
            selectedItem.visible = false;
            removeButton.visible = false;
            selectedItem = null;
            listItems();
        } catch (IOException ioException) {
            textFieldSubMessage = RED + ioException.getMessage();
        } catch (NullPointerException ignored) {

        }
    }

    private void listItems() {
        listItemButtons.clear();
        this.buttonList.removeIf(guiButton -> guiButton instanceof ListItemButton);

        for (String element : lifeKnightStringList.getValue()) {
            if (searchInput.isEmpty() || element.toLowerCase().contains(searchInput.toLowerCase())) {
                ListItemButton listItemButton = new ListItemButton(listItemButtons.size() + 6, element) {
                    @Override
                    public void work() {
                        if (this.isSelectedButton) {
                            this.isSelectedButton = false;
                            selectedItem = null;
                            removeButton.visible = false;
                        } else {
                            this.isSelectedButton = true;
                            selectedItem = this;
                            removeButton.visible = true;
                        }
                    }
                };
                listItemButtons.add(listItemButton);
            }
        }
        listMessage = listItemButtons.size() == 0 ? GRAY + "No items found" : "";

        clearButton.visible = listItemButtons.size() > 1;

        this.buttonList.addAll(listItemButtons);
    }
}
