package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.hud.EnhancedHudText;
import com.lifeknight.combatanalysis.utilities.*;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import com.lifeknight.combatanalysis.variables.LifeKnightList;
import com.lifeknight.combatanalysis.variables.LifeKnightNumber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.util.EnumChatFormatting.GOLD;

@net.minecraftforge.fml.common.Mod(modid = Core.MOD_ID, name = Core.MOD_NAME, version = Core.MOD_VERSION, clientSideOnly = true)
public class Core {
    public static final String
            MOD_NAME = "Combat Analysis",
            MOD_VERSION = "0.2.15",
            MOD_ID = "combatanalysis";
    public static final EnumChatFormatting MOD_COLOR = GOLD;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new LifeKnightThreadFactory());
    public static GuiScreen guiToOpen = null;
    public static final LifeKnightBoolean runMod = new LifeKnightBoolean("Mod", "Main", true) {
        @Override
        public void onSetValue() {
            CombatSession.onWorldLoad();
        }
    };
    public static final LifeKnightBoolean gridSnapping = new LifeKnightBoolean("Grid Snapping", "HUD", true);
    public static final LifeKnightBoolean hudTextShadow = new LifeKnightBoolean("HUD Text Shadow", "HUD", true);
    private static final LifeKnightBoolean showStatus = new LifeKnightBoolean("Show Status", "HUD", true);
    public static final LifeKnightBoolean automaticSessions = new LifeKnightBoolean("Automatic Sessions", "Settings", true);
    public static final LifeKnightBoolean allAutoEnd = new LifeKnightBoolean("End For All End Requirements", "Auto End", true);
    public static final LifeKnightBoolean endOnGameEnd = new LifeKnightBoolean("End On Game End", "Auto End", true);
    public static final LifeKnightBoolean endOnSpectator = new LifeKnightBoolean("End On Spectator", "Auto End", true);
    public static final LifeKnightBoolean automaticallyLogSessions = new LifeKnightBoolean("Auto-Log Sessions", "Settings", true);
    public static final LifeKnightNumber.LifeKnightInteger mainHotBarSlot = new LifeKnightNumber.LifeKnightInteger("Main Hotbar Slot", "Settings", 1, 1, 9);
    public static final KeyBinding toggleCombatSessionKeyBinding = new KeyBinding("Toggle combat session", 0x1B, MOD_NAME);
    public static final KeyBinding openLatestCombatSessionKeyBinding = new KeyBinding("Open latest combat session", 0x26, MOD_NAME);
    public static final LifeKnightList.LifeKnightIntegerList deletedSessionIds = new LifeKnightList.LifeKnightIntegerList("Deleted Session IDs", "Extra");
    public static final LifeKnightList.LifeKnightIntegerList wonSessionIds = new LifeKnightList.LifeKnightIntegerList("Won Session IDs", "Extra");
    public static final LifeKnightList.LifeKnightIntegerList loggedSessionIds = new LifeKnightList.LifeKnightIntegerList("Logged Session IDs", "Extra");
    public static final Logger combatSessionLogger = new Logger(new File("logs/lifeknight/combatsessions"));
    private static final List<Long> leftClicks = new ArrayList<>();
    public static Configuration configuration;
    /*
    How to deal with lava and fire
    alt text, scoreboard title?
    */

    @EventHandler
    public void initialize(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ModCommand());
        ClientRegistry.registerKeyBinding(toggleCombatSessionKeyBinding);
        ClientRegistry.registerKeyBinding(openLatestCombatSessionKeyBinding);

        deletedSessionIds.setShowInLifeKnightGui(false);
        wonSessionIds.setShowInLifeKnightGui(false);
        loggedSessionIds.setShowInLifeKnightGui(false);

        Miscellaneous.createEnhancedHudTextDefaultPropertyVariables();

        this.createEnhancedHudTexts();

        configuration = new Configuration();

        CombatSession.getLoggedCombatSessions();
    }

    private void createEnhancedHudTexts() {
        new EnhancedHudText("Melee Accuracy", 0, 0, "Melee Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.meleeAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Bow Accuracy", 0, 100, "Bow Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.arrowAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Projectile Accuracy", 0, 200, "Projectile Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.projectileAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Hits Dealt", 0, 300, "Hits Dealt") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.hitsDealt());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Hits Taken", 0, 400, "Hits Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.hitsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Arrows Hit", 0, 500, "Arrows Hit") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.arrowsHit());
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

        new EnhancedHudText("CPS", 0, 700, "CPS") {
            @Override
            public String getTextToDisplay() {
                leftClicks.removeIf(time -> time < System.currentTimeMillis() - 1000L);
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
            }
        }, 1000);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        CombatSession.onWorldLoad();
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.entityPlayer.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID() && runMod.getValue() && event.target instanceof EntityPlayer) {
            CombatSession.onAttack((EntityPlayer) event.target);
        }
    }

    // Injected
    public static void onLivingHurt(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityPlayer) || !runMod.getValue()) return;
        if (entityLivingBase.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            CombatSession.onHurt(null);
        } else {
            CombatSession.onPlayerHurt((EntityPlayer) entityLivingBase);
        }
    }

    @SubscribeEvent
    public void onArrowShot(ArrowLooseEvent event) {
        if (runMod.getValue() && event.charge >= 3) {
            CombatSession.onArrowShot();
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null) return;
        if (runMod.getValue() && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            Item item = Minecraft.getMinecraft().thePlayer.getHeldItem().getItem();
            if (item instanceof ItemFishingRod && Minecraft.getMinecraft().thePlayer.fishEntity == null) {
                CombatSession.onProjectileThrown(true);
            } else if (item instanceof ItemEgg || item instanceof ItemSnowball) {
                CombatSession.onProjectileThrown(false);
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
        if (event.phase == TickEvent.Phase.END && runMod.getValue()) Manipulable.renderManipulables();
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