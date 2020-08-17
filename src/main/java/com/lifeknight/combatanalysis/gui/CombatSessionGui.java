package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.mod.CombatSession;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;

import static net.minecraft.util.EnumChatFormatting.*;

public class CombatSessionGui extends PanelGui {
    private final CombatSession combatSession;
    private final String panelMessage;
    private final List<CombatSession> combatSessions;
    private final int index;
    private final int combatSessionSize;

    public CombatSessionGui(CombatSession combatSession) {
        this.combatSession = combatSession;
        this.panelMessage = combatSession == null ? GRAY + "There is no combat session to display" : "";
        this.combatSessions = CombatSession.getCombatSessionsForGui();
        this.index = combatSession == null ? Integer.MIN_VALUE : this.combatSessions.indexOf(combatSession);
        this.combatSessionSize = this.combatSessions.size();
        this.name = "Combat Session";
    }

    @Override
    public void initGui() {
        super.guiPanels.clear();

        this.buttonList.add(new LifeKnightButton("Filter", this.buttonList.size(), this.width - 200 - 20, 25, 100) {
            @Override
            public void work() {
                Core.openGui(new CombatSessionFilterGui(CombatSessionGui.this));
            }
        });

        if (this.combatSession != null) {
            if (!this.combatSession.isLogged()) {
                this.buttonList.add(new LifeKnightButton("Log", this.buttonList.size(), this.width - 200 - 20 - 40 - 10, 5, 40) {
                    @Override
                    public void work() {
                        CombatSessionGui.this.combatSession.log();
                        CombatSessionGui.this.buttonList.remove(this);
                    }
                });
            }
            if (this.index > 0) {
                this.buttonList.add(new LifeKnightButton("<", this.buttonList.size(), this.width - 200 - 20, 5, 20) {
                    @Override
                    public void work() {
                        Core.openGui(new CombatSessionGui(CombatSessionGui.this.combatSessions.get(CombatSessionGui.this.index - 1)));
                    }
                });
            }
            if (this.index < this.combatSessionSize - 1) {
                this.buttonList.add(new LifeKnightButton(">", this.buttonList.size(), this.width - 140, 5, 20) {
                    @Override
                    public void work() {
                        Core.openGui(new CombatSessionGui(CombatSessionGui.this.combatSessions.get(CombatSessionGui.this.index + 1)));
                    }
                });
            }

            List<String> basicData = new ArrayList<>();

            basicData.add("Server: " + this.combatSession.getServerIp());
            basicData.add("Date: " + Miscellaneous.getTimeAndDate(this.combatSession.getStartTime()));
            basicData.add("Duration: " + Text.formatTimeFromMilliseconds(this.combatSession.getTime(), 2));
            basicData.add("Detected Type: " + this.combatSession.detectType());
            basicData.add("");
            basicData.add("Version: " + this.combatSession.getVersion());

            super.createListPanel("Details", basicData).setColor(Color.RED);

            super.createButtonPanel("Properties", Arrays.asList(
                    new LifeKnightButton.VersatileLifeKnightButton(this.combatSession.isWon() ? GREEN + "Won" : RED + "Lost", versatileLifeKnightButton -> {
                        try {
                            if (CombatSessionGui.this.combatSession.isWon()) {
                                Core.wonSessionIds.removeElement(CombatSessionGui.this.combatSession.getId());
                                CombatSessionGui.this.combatSession.setWon(false);
                            } else {
                                Core.wonSessionIds.addElement(CombatSessionGui.this.combatSession.getId());
                                CombatSessionGui.this.combatSession.setWon(true);
                            }
                            versatileLifeKnightButton.displayString = CombatSessionGui.this.combatSession.isWon() ? GREEN + "Won" : RED + "Lost";
                        } catch (Exception exception) {
                            Miscellaneous.logError("Tried to remove or add combat session from the won-id list: %s", exception.getMessage());
                        }
                    }), new LifeKnightButton.VersatileLifeKnightButton(this.combatSession.isDeleted() ? RED + "Deleted" : GREEN + "Available", versatileLifeKnightButton -> {
                        try {
                            if (CombatSessionGui.this.combatSession.isDeleted()) {
                                Core.deletedSessionIds.removeElement(CombatSessionGui.this.combatSession.getId());
                                CombatSessionGui.this.combatSession.setDeleted(false);
                            } else {
                                Core.deletedSessionIds.addElement(CombatSessionGui.this.combatSession.getId());
                                CombatSessionGui.this.combatSession.setDeleted(true);
                            }
                            versatileLifeKnightButton.displayString = CombatSessionGui.this.combatSession.isDeleted() ? RED + "Deleted" : GREEN + "Available";
                        } catch (Exception exception) {
                            Miscellaneous.logError("Tried to remove or add combat session from the deleted-id list: %s", exception.getMessage());
                        }
                    })));

            List<String> overall = new ArrayList<>();
            overall.add("Starting Health: " + Text.shortenDouble(this.combatSession.getStartingHealth(), 1));
            overall.add("Ending Health: " + Text.shortenDouble(this.combatSession.getEndingHealth(), 1));
            overall.add("");
            overall.add("Left Clicks: " + this.combatSession.getLeftClicks());
            overall.add("Right Clicks: " + this.combatSession.getRightClicks());
            overall.add("Average CPS: " + this.combatSession.getAverageClicksPerSecond());
            overall.add("");
            overall.add("Melee Accuracy: " + this.combatSession.getMeleeAccuracy());
            overall.add("Arrow Accuracy: " + this.combatSession.getArrowAccuracy());
            overall.add("Projectile Accuracy: " + this.combatSession.getProjectileAccuracy());
            overall.add("");
            overall.add("Melee Hits Taken: " + this.combatSession.getHitsTaken());
            overall.add("Arrows Taken: " + this.combatSession.getArrowsTaken());
            overall.add("Projectiles Taken: " + this.combatSession.getProjectilesTaken());

            super.createListPanel("Overall Stats", overall).setColor(Color.BLUE);

            List<String> strafing = new ArrayList<>();

            for (CombatSession.StrafingTracker strafingTracker : this.combatSession.getStrafes()) {
                strafing.add((strafingTracker.isRightStrafe() ? ">" : "<") + " - " + Text.formatTimeFromMilliseconds(strafingTracker.getTime(), 0) + "ms");
            }

            super.createListPanel("Strafes", strafing.size() != 0 ? strafing : Collections.singletonList("There are no strafes to display."));

            PotionEffectPanel potionEffectPanel = new PotionEffectPanel("Potion Effects", this.combatSession.getPotionEffects(), this.combatSession.getStartTime());
            potionEffectPanel.setColor(Color.ORANGE);
            super.guiPanels.add(potionEffectPanel);

            HotKeyPanel hotKeyPanel = new HotKeyPanel("HotKeys", this.combatSession.getHotKeys());
            hotKeyPanel.setColor(Color.MAGENTA);
            super.guiPanels.add(hotKeyPanel);

            InventoryPanel startingArmor = new InventoryPanel("Starting Armor", this.combatSession.getStartingArmor());
            startingArmor.setColor(Color.GREEN);
            super.guiPanels.add(startingArmor);

            InventoryPanel startingInventory = new InventoryPanel("Starting Inventory", this.combatSession.getStartingInventory());
            startingInventory.setColor(Color.GREEN);
            super.guiPanels.add(startingInventory);

            InventoryPanel endingArmor = new InventoryPanel("Ending Armor", this.combatSession.getEndingArmor());
            endingArmor.setColor(Color.GREEN);
            super.guiPanels.add(endingArmor);

            InventoryPanel endingInventory = new InventoryPanel("Ending Inventory", this.combatSession.getEndingInventory());
            endingInventory.setColor(Color.GREEN);
            super.guiPanels.add(endingInventory);

            for (CombatSession.OpponentTracker opponentTracker : this.combatSession.getOpponentTrackerMap()) {
                List<String> data = new ArrayList<>();
                data.add("Melee Hits: " + opponentTracker.getOpponentHitsTaken());
                data.add("Critical Hits Dealt: " + opponentTracker.getCriticalHitsLanded());
                data.add("Times Shot: " + opponentTracker.getArrowsHit());
                data.add("Projectile Hits: " + opponentTracker.getProjectilesHit());
                data.add("");
                data.add("Melee Accuracy: " + opponentTracker.attackAccuracy());
                data.add("");
                data.add("Melee Hits Taken: " + opponentTracker.getHitsTaken());
                data.add("Critical Hits Taken: " + opponentTracker.getCriticalHitsTaken());
                data.add("Shots Taken: " + opponentTracker.getArrowsTaken());
                data.add("Projectiles Taken: " + opponentTracker.getProjectilesTaken());
                super.createListPanel(opponentTracker.getName(), data).setColor(Color.YELLOW);

                InventoryPanel opponentStartingArmor = new InventoryPanel(opponentTracker.getName() + " - Armor", opponentTracker.getOpponentStartingArmor());
                opponentStartingArmor.setColor(Color.BLACK);
                super.guiPanels.add(opponentStartingArmor);

                List<String> combos = new ArrayList<>();
                for (CombatSession.ComboTracker comboTracker : opponentTracker.getComboTrackers()) {
                    combos.add(Text.formatTimeFromMilliseconds(comboTracker.getTime(), 2) + " - " + comboTracker.getComboCount());
                }
                super.createListPanel("Combos - " + opponentTracker.getName(), combos.size() == 0 ? Collections.singletonList("No combos to display.") : combos).setColor(Color.CYAN);
            }
        }
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.panelMessage, this.width / 2, (this.height - 55) / 2 + 55, 0xffffffff);
        if (this.combatSession != null) {
            this.drawCenteredString(this.fontRendererObj, (this.index + 1) + " / " + this.combatSessionSize, this.width - 170, 11, 0xffffffff);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static class HotKeyPanel extends GuiPanel {
        private final List<CombatSession.HotKeyTracker> hotKeyTrackers;

        public HotKeyPanel(String name, List<CombatSession.HotKeyTracker> hotKeyTrackers) {
            super(0, 0, name);
            this.hotKeyTrackers = hotKeyTrackers;
            this.versatile = true;
            this.xPosition = Integer.MIN_VALUE;
            this.yPosition = Integer.MIN_VALUE;
            this.updateDimensions();
            this.updateOriginals();
        }

        @Override
        protected int getPanelHeight() {
            return Math.max(30, this.hotKeyTrackers.size() * 20 + 5);
        }

        @Override
        protected int getPanelWidth() {
            int longestWidth = 0;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

            if (this.hotKeyTrackers.size() == 0) {
                return Math.max(fontRenderer.getStringWidth(this.name) + 15, fontRenderer.getStringWidth("No hot keys to display.") + 10);
            }

            for (CombatSession.HotKeyTracker hotKeyTracker : this.hotKeyTrackers) {
                int width = 16 +
                        fontRenderer.getStringWidth(" - " + Text.formatTimeFromMilliseconds(hotKeyTracker.getTime(), 0) + "ms");
                longestWidth = Math.max(longestWidth, width);
            }
            return Math.max(fontRenderer.getStringWidth(this.name) + 15, longestWidth + 15);
        }

        @Override
        public void drawPanel() {
            super.drawPanel();
            if (this.visible && this.yPosition + this.height > 56) {
                GlStateManager.pushMatrix();
                this.scissor();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                if (this.hotKeyTrackers.size() == 0) {
                    fontRenderer.drawString("No hot keys to display.", this.xPosition + 5 + this.xOffsetPosition, this.yPosition + 14 + 2 + this.yOffsetPosition, 0xffffffff);
                } else {
                    for (int i = 0; i < this.hotKeyTrackers.size(); i++) {
                        CombatSession.HotKeyTracker hotKeyTracker = this.hotKeyTrackers.get(i);
                        if (hotKeyTracker.getItemStack() != null) {
                            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(
                                    hotKeyTracker.getItemStack(), this.xPosition + 5 + this.xOffsetPosition, this.yPosition + 14 + i * 20 + +this.yOffsetPosition
                            );
                        }
                        fontRenderer.drawString(" - " + Text.formatTimeFromMilliseconds(hotKeyTracker.getTime(), 0) + "ms", this.xPosition + 5 + this.xOffsetPosition + 16, this.yPosition + 14 + i * 20 + 7 + this.yOffsetPosition, 0xffffffff);
                    }
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }
        }
    }

    private static class InventoryPanel extends GuiPanel {
        private final Map<ItemStack, Integer> itemStacks;

        public InventoryPanel(String name, Map<ItemStack, Integer> itemStacks) {
            super(0, 0, name);
            this.itemStacks = itemStacks;
            this.updateDimensions();
            this.updateOriginals();
        }

        @Override
        protected int getPanelWidth() {
            int longestWidth = 0;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

            if (this.itemStacks.size() == 0) {
                return Math.max(fontRenderer.getStringWidth(this.name) + 15, fontRenderer.getStringWidth("No items to display.") + 10);
            }

            for (ItemStack itemStack : this.itemStacks.keySet()) {
                int stackSize = this.itemStacks.get(itemStack);
                String description = " - " + stackSize + "x " + (itemStack.getMaxDamage() == 0 ? "" : (itemStack.getMaxDamage() - itemStack.getItemDamage()) + " / " + itemStack.getMaxDamage());

                int width = 16 +
                        fontRenderer.getStringWidth(description);
                longestWidth = Math.max(longestWidth, width);
            }
            return Math.max(fontRenderer.getStringWidth(this.name), longestWidth) + 15;
        }

        @Override
        protected int getPanelHeight() {
            return Math.max(30, this.itemStacks.size() * 20 + 5);
        }

        @Override
        public void drawPanel() {
            super.drawPanel();
            if (this.visible && this.yPosition + this.height > 56) {
                GlStateManager.pushMatrix();
                this.scissor();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                if (this.itemStacks.size() == 0) {
                    fontRenderer.drawString("No items to display.", this.xPosition + 5 + this.xOffsetPosition, this.yPosition + 14 + 2 + this.yOffsetPosition, 0xffffffff);
                } else {
                    List<ItemStack> itemStacks = new ArrayList<>(this.itemStacks.keySet());
                    for (int i = 0; i < itemStacks.size(); i++) {
                        ItemStack itemStack = itemStacks.get(i);
                        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(
                                itemStack, this.xPosition + 5 + this.xOffsetPosition, this.yPosition + 14 + i * 20 + 2 + this.yOffsetPosition
                        );
                        int stackSize = this.itemStacks.get(itemStack);
                        String description = " - " + stackSize + "x " + (itemStack.getMaxDamage() == 0 ? "" : (itemStack.getMaxDamage() - itemStack.getItemDamage()) + " / " + itemStack.getMaxDamage());

                        fontRenderer.drawString(description, this.xPosition + 5 + this.xOffsetPosition + 16, this.yPosition + 14 + i * 20 + 7 + this.yOffsetPosition, 0xffffffff);
                    }
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }
        }
    }

    private static class PotionEffectPanel extends GuiPanel {
        private final List<CombatSession.PotionEffectTracker> potionEffectTrackers;
        private final long combatSessionStartTime;

        public PotionEffectPanel(String name, List<CombatSession.PotionEffectTracker> potionEffectTrackers, long combatSessionStartTime) {
            super(0, 0, name);
            this.potionEffectTrackers = potionEffectTrackers;
            this.combatSessionStartTime = combatSessionStartTime;
            this.updateDimensions();
            this.updateOriginals();
        }

        @Override
        protected int getPanelWidth() {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            if (this.potionEffectTrackers.size() == 0) {
                return Math.max(fontRenderer.getStringWidth(this.name) + 15, fontRenderer.getStringWidth("No potion effects to display.") + 10);
            }

            int longestWidth = 0;
            for (CombatSession.PotionEffectTracker potionEffectTracker : this.potionEffectTrackers) {
                PotionEffect potionEffect = potionEffectTracker.getPotionEffect();
                longestWidth = Math.max(longestWidth, Math.max(
                        fontRenderer.getStringWidth(
                                Text.formatTimeFromMilliseconds(potionEffectTracker.getStartTime() - this.combatSessionStartTime, 2) +
                                        " (" + StringUtils.ticksToElapsedTime(potionEffectTracker.getTotalDuration()) + ")"),
                        fontRenderer.getStringWidth(potionEffect.getEffectName())) + 25);
            }
            return Math.max(fontRenderer.getStringWidth(this.name) + 15, longestWidth + 10);
        }

        @Override
        protected int getPanelHeight() {
            return Math.max(30, this.potionEffectTrackers.size() * 25 + 5);
        }

        @Override
        public void drawPanel() {
            super.drawPanel();
            if (this.visible && this.yPosition + this.height > 56) {
                GlStateManager.pushMatrix();
                this.scissor();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

                int x = this.xPosition + 5 + this.xOffsetPosition;
                int y = this.yPosition + 14 + 2 + this.yOffsetPosition;

                if (this.potionEffectTrackers.size() == 0) {
                    fontRenderer.drawString("No potion effects to display.", x, y, 0xffffffff);
                } else {
                    for (int i = 0; i < this.potionEffectTrackers.size(); i++) {
                        CombatSession.PotionEffectTracker potionEffectTracker = this.potionEffectTrackers.get(i);
                        PotionEffect potionEffect = potionEffectTracker.getPotionEffect();
                        Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                        if (!potion.shouldRender(potionEffect)) continue;
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                        if (potion.hasStatusIcon()) {
                            int i1 = potion.getStatusIconIndex();
                            super.drawTexturedModalRect(x + 1, y + 1 + 25 * i, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        }
                        potion.renderInventoryEffect(x + 1, y + 1 + 25 * i, potionEffect, Minecraft.getMinecraft());
                        String s1 = I18n.format(potion.getName());

                        if (potionEffect.getAmplifier() == 1) {
                            s1 = s1 + " " + I18n.format("enchantment.level.2");
                        } else if (potionEffect.getAmplifier() == 2) {
                            s1 = s1 + " " + I18n.format("enchantment.level.3");
                        } else if (potionEffect.getAmplifier() == 3) {
                            s1 = s1 + " " + I18n.format("enchantment.level.4");
                        }

                        fontRenderer.drawString(s1, x + 22, y + 25 * i, 0xffffffff);
                        String s = Text.formatTimeFromMilliseconds(potionEffectTracker.getStartTime() - this.combatSessionStartTime, 2) + " (" + StringUtils.ticksToElapsedTime(potionEffectTracker.getTotalDuration()) + ")";
                        fontRenderer.drawString(s, x + 22, y + 25 * i + 10, 0xffffffff);
                    }
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            }
        }
    }
}
