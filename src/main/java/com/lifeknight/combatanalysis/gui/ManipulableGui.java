package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.gui.components.ManipulableButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lifeknight.combatanalysis.gui.Manipulable.manipulableComponents;

public class ManipulableGui extends GuiScreen {
    public static final List<ManipulableButton> manipulableButtons = new ArrayList<>();

    @Override
    public void initGui() {
        manipulableButtons.clear();
        for (Manipulable manipulable : manipulableComponents) {
            manipulableButtons.add(new ManipulableButton(manipulable));
        }
        super.buttonList.addAll(manipulableButtons);

        for (Manipulable manipulable : manipulableComponents) {
            for (Object component : manipulable.connectedComponents) {
                if (component instanceof GuiButton) {
                    super.buttonList.add((GuiButton) component);
                }
            }
        }

        super.buttonList.add(new LifeKnightButton(super.buttonList.size(), this.width / 2 - 50, this.height - 30, 100, 20, "Reset") {
            @Override
            public void work() {
                for (GuiButton guiButton : ManipulableGui.super.buttonList) {
                    if (guiButton instanceof ManipulableButton) {
                        ((ManipulableButton) guiButton).reset();
                    }
                }
            }
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof LifeKnightButton) ((LifeKnightButton) button).work();
    }
}
