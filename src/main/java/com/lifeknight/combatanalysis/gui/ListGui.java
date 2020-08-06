package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.*;
import com.lifeknight.combatanalysis.utilities.Video;
import com.lifeknight.combatanalysis.variables.LifeKnightList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.openGui;
import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.RED;

public class ListGui extends GuiScreen {
    private final List<ListItemButton> listItemButtons = new ArrayList<>();
    private final LifeKnightList lifeKnightList;
    private ConfirmButton clearButton;
    private ScrollBar scrollBar;
    private LifeKnightTextField addField, searchField;
    public ListItemButton selectedItem;
    public LifeKnightButton addButton, removeButton;
    private String searchInput = "", listMessage = "";
    public GuiScreen lastGui;

    public ListGui(LifeKnightList<?> lifeKnightList) {
        this.lifeKnightList = lifeKnightList;
        lastGui = null;
    }

    public ListGui(LifeKnightList<?> lifeKnightList, GuiScreen lastGui) {
        this(lifeKnightList);
        this.lastGui = lastGui;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(fontRendererObj, listMessage, Video.get2ndPanelCenter(), super.height / 2, 0xffffffff);
        drawVerticalLine(Video.getScaledWidth(300), 0, super.height, 0xffffffff);
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
        searchField = new LifeKnightTextField(0, Video.getScaledWidth(75), this.height - 40, Video.getScaledWidth(150), 20, "Search") {
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

        addField = new LifeKnightTextField(1, Video.getScaledWidth(75), Video.getScaledHeight(85), Video.getScaledWidth(150), 20, lifeKnightList.getName()) {
            @Override
            public void handleInput() {
                this.lastInput = this.getText();
                this.setText("");
                try {
                    if (!lastInput.isEmpty()) {
                        //noinspection unchecked
                        lifeKnightList.addElement(lifeKnightList.fromFormattedString(lastInput));
                        setSubDisplayMessage("");
                        listItems();
                    }
                } catch (IOException ioException) {
                    setSubDisplayMessage(RED + ioException.getMessage());
                } catch (Exception exception) {
                    setSubDisplayMessage(RED + "Invalid input!");
                }
            }
        };

        super.buttonList.add(addButton = new LifeKnightButton("Add", 2, Video.getScaledWidth(75), Video.getScaledHeight(165), Video.getScaledWidth(150)) {
            @Override
            public void work() {
                addField.handleInput();
                selectedItem = null;
            }
        });

        super.buttonList.add(removeButton = new LifeKnightButton("Remove", 3, Video.getScaledWidth(75), Video.getScaledHeight(230), Video.getScaledWidth(150)) {
            @Override
            public void work() {
                removeSelectedButton();
            }
        });
        removeButton.visible = false;

        super.buttonList.add(clearButton = new ConfirmButton(4, Video.getScaledWidth(75), Video.getScaledHeight(295), Video.getScaledWidth(150), "Clear", RED + "Confirm") {
            @Override
            public void onConfirm() {
                lifeKnightList.clear();
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

        if (lastGui != null) {
            super.buttonList.add(new LifeKnightButton("Back", 5, 5, 5, 50) {
                @Override
                public void work() {
                    openGui(lastGui);
                }
            });
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
        boolean aButtonHasBeenSelected = false;
        for (ListItemButton listItemButton : listItemButtons) {
            if (listItemButton.isSelectedButton) {
                aButtonHasBeenSelected = true;
                break;
            }
        }
        removeButton.visible = aButtonHasBeenSelected;
    }

    protected void removeSelectedButton() {
        try {
            //noinspection unchecked
            lifeKnightList.removeElement(lifeKnightList.getMap().inverse().get(selectedItem.displayString));
            selectedItem.visible = false;
            removeButton.visible = false;
            selectedItem = null;
            listItems();
        } catch (Exception exception) {
           addField.setSubDisplayMessage(RED + exception.getMessage());
        }
    }

    private void listItems() {
        listItemButtons.clear();
        this.buttonList.removeIf(guiButton -> guiButton instanceof ListItemButton);

        for (Object element : lifeKnightList.getValue()) {
            if (searchInput.isEmpty() || ((String) lifeKnightList.getMap().get(element)).toLowerCase().contains(searchInput.toLowerCase())) {
                ListItemButton listItemButton = new ListItemButton(listItemButtons.size() + 6, (String) lifeKnightList.getMap().get(element)) {
                    @Override
                    public void work() {
                        if (this.isSelectedButton) {
                            this.isSelectedButton = false;
                            selectedItem = null;
                        } else {
                            this.isSelectedButton = true;
                            selectedItem = this;
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
