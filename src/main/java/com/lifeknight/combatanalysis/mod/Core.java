package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.hud.EnhancedHudText;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Logger;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import com.lifeknight.combatanalysis.variables.LifeKnightList;
import com.lifeknight.combatanalysis.variables.LifeKnightNumber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.util.EnumChatFormatting.GOLD;

@net.minecraftforge.fml.common.Mod(modid = Core.MOD_ID, name = Core.MOD_NAME, version = Core.MOD_VERSION, clientSideOnly = true)
public class Core {
    public static final String
            MOD_NAME = "Combat Analysis",
            MOD_VERSION = "0.1",
            MOD_ID = "combatanalysis";
    public static final EnumChatFormatting MOD_COLOR = GOLD;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new LifeKnightThreadFactory());
    public static boolean onHypixel = false;
    public static GuiScreen guiToOpen = null;
    public static final LifeKnightBoolean runMod = new LifeKnightBoolean("Core", "Main", true);
    public static final LifeKnightBoolean gridSnapping = new LifeKnightBoolean("Grid Snapping", "HUD", true);
    public static final LifeKnightBoolean hudTextShadow = new LifeKnightBoolean("HUD Text Shadow", "HUD", true);
    public static final LifeKnightBoolean showStatus = new LifeKnightBoolean("Show Status", "HUD", true);
    public static final LifeKnightBoolean automaticSessions = new LifeKnightBoolean("Automatic Sessions", "Settings", true);
    public static final LifeKnightBoolean logSessions = new LifeKnightBoolean("Log Sessions", "Settings", true);
    public static final LifeKnightNumber.LifeKnightInteger mainHotBarSlot = new LifeKnightNumber.LifeKnightInteger("Main HotBar Slot", "Settings", 1, 1, 9);
    public static final LifeKnightList.LifeKnightIntegerList deletedSessionIds = new LifeKnightList.LifeKnightIntegerList("Deleted Session IDs", "Extra");
    public static final Logger combatSessionLogger = new Logger(new File("logs/combatsessions"));
    private static final List<Long> leftClicks = new ArrayList<>();
    public static Configuration configuration;

    /*
    Make hotkey show multiple of same item
    Remove strafe entries that are 0 long
    Change text time format to have argument for time measures (hours, minutes, etc.)
    Fix Hotkey display, match inventory armor
    Settings button under navigate, opening GUI where you can change the things that appear or search for certain analyses
    toString, interpret from json
    Add clicks if necessary for CombatSession when activated
    */

    @EventHandler
    public void init(FMLInitializationEvent initEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ModCommand());

        deletedSessionIds.setShowInLifeKnightGui(false);

        Miscellaneous.createEnhancedHudTextDefaultPropertyVariables();
        
        this.createEnhancedHudTexts();

        configuration = new Configuration();

        CombatSession.readLoggedCombatSessions();
    }

    private void createEnhancedHudTexts() {
        new EnhancedHudText("Left Clicks", 0, 0, "Left Clicks") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.leftClicks());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Right Clicks", 0, 100, "Right Clicks") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.rightClicks());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Melee Accuracy", 0, 200, "Melee Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.attackAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Bow Accuracy", 0, 300, "Bow Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.arrowAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Projectile Accuracy", 0, 400, "Projectile Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.projectileAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Hits Taken", 0, 500, "Hits Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.hitsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Arrows Taken", 0, 600, "Arrows Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.arrowsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Projectiles Taken", 0, 700, "Projectiles Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.projectilesTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("CPS", 0, 700, "CPS") {
            @Override
            public String getTextToDisplay() {
                leftClicks.removeIf( time -> time < System.currentTimeMillis() - 1000L);
                return String.valueOf(leftClicks.size());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };
    }

    @SubscribeEvent
    public void onConnect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Chat.sendQueuedChatMessages();
                onHypixel = !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net");
            }
        }, 1000);
    }

    @SubscribeEvent
    public void onChatMessageReceived(ClientChatReceivedEvent event) {

    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        CombatSession.onWorldLoad();
    }

    public static void onHurt(EntityPlayer attacker) {
        CombatSession.onHurt(attacker);
    }

    // Injected
    public static void onAttackEntityOtherPlayerMPFrom(EntityOtherPlayerMP entityOtherPlayerMP, DamageSource damageSource) {
        if (damageSource.getEntity() == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (damageSource.getEntity().getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            switch (damageSource.getDamageType()) {
                case "player":
                    CombatSession.onAttack(entityOtherPlayerMP);
                    break;
                case "arrow":
                    CombatSession.onArrowHit(entityOtherPlayerMP);
                    break;
                case "thrown":
                    CombatSession.onProjectileHit(entityOtherPlayerMP);
                    break;
            }
        }
    }
    // Injected
    public static void onAttackEntityPlayerSPFrom(DamageSource damageSource) {
        if (!(damageSource.getEntity() instanceof EntityPlayer) || !runMod.getValue()) return;
        switch (damageSource.getDamageType()) {
            case "player":
                CombatSession.onHurt((EntityPlayer) damageSource.getEntity());
                break;
            case "arrow":
                CombatSession.onHitByArrow((EntityPlayer) damageSource.getEntity());
                break;
            case "thrown":
                CombatSession.onHitByProjectile((EntityPlayer) damageSource.getEntity());
                break;
        }
    }

    // Injected
    public static void onLivingHurt(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityPlayer)) return;
        if (entityLivingBase.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            CombatSession.onHurt(null);
        } else {
            CombatSession.onPlayerHurt((EntityPlayer) entityLivingBase);
        }
    }

    @SubscribeEvent
    public void onArrowShot(ArrowLooseEvent event) {
        if (runMod.getValue() && event.charge > 2) {
            CombatSession.onArrowShot();
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null) return;
        if (runMod.getValue() && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            Item item = Minecraft.getMinecraft().thePlayer.getHeldItem().getItem();
            if (item instanceof ItemFishingRod && Minecraft.getMinecraft().thePlayer.fishEntity == null) {
                CombatSession.onProjectileThrown(0);
            } else if (item instanceof ItemEgg) {
                CombatSession.onProjectileThrown(1);
            } else if (item instanceof ItemSnowball) {
                CombatSession.onProjectileThrown(2);
            }
        }
    }

    @SubscribeEvent
    public void onMousePressed(InputEvent.MouseInputEvent event) {
        if (runMod.getValue() && Mouse.getEventButtonState()) {
            if (Mouse.getEventButton() == 0) {
                CombatSession.onLeftClick();
                leftClicks.add(System.currentTimeMillis());
            } else if (Mouse.getEventButton() == 1) {
                CombatSession.onRightClick();
            }
        }
    }

    @SubscribeEvent
    public void onKeyTyped(InputEvent.KeyInputEvent event) {
        CombatSession.onKeyTyped(Keyboard.getEventKey(), Keyboard.getEventKeyState());
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (guiToOpen != null) {
            Minecraft.getMinecraft().displayGuiScreen(guiToOpen);
            guiToOpen = null;
        }
        Manipulable.renderManipulables();
    }

    public static void openGui(GuiScreen guiScreen) {
        guiToOpen = guiScreen;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (runMod.getValue() && event.phase == TickEvent.Phase.END) {
            CombatSession.onTick();
        }
    }
}