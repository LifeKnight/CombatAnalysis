package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.hud.EnhancedHudText;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.util.EnumChatFormatting.GOLD;

@net.minecraftforge.fml.common.Mod(modid = Core.modId, name = Core.modName, version = Core.modVersion, clientSideOnly = true)
public class Core {
    public static final String
            modName = "Combat Analysis",
            modVersion = "1.0",
            modId = "combatanalysis";
    public static final EnumChatFormatting modColor = GOLD;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new LifeKnightThreadFactory());
    public static boolean onHypixel = false;
    public static GuiScreen guiToOpen = null;
    public static final LifeKnightBoolean runMod = new LifeKnightBoolean("Core", "Main", true);
    public static final LifeKnightBoolean gridSnapping = new LifeKnightBoolean("Grid Snapping", "HUD", true);
    public static final LifeKnightBoolean hudTextShadow = new LifeKnightBoolean("HUD Text Shadow", "HUD", true);
    public static final LifeKnightBoolean showStatus = new LifeKnightBoolean("Show Status", "HUD", true);
    public static final LifeKnightBoolean automaticSessions = new LifeKnightBoolean("Automatic Sessions", "Settings", true);
    public static final LifeKnightNumber.LifeKnightInteger mainHotBarSlot = new LifeKnightNumber.LifeKnightInteger("Main Hotbar Slot", "Settings", 1, 1, 9);
    public static Configuration configuration;

    @EventHandler
    public void init(FMLInitializationEvent initEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ModCommand());

        Miscellaneous.createEnhancedHudTextDefaultPropertyVariables();
        
        createEnhancedHudTexts();

        configuration = new Configuration();
    }

    private void createEnhancedHudTexts() {
        new EnhancedHudText("Left Clicks", 0, 0, "Left Clicks") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getLeftClicks());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Right Clicks", 0, 100, "Right Clicks") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getRightClicks());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Opponent Hits Taken", 0, 200, "Opponent Hits Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getOpponentHitsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Melee Accuracy", 0, 300, "Melee Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.getAttackAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Bow Accuracy", 0, 400, "Bow Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.getArrowAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Projectile Accuracy", 0, 500, "Projectile Accuracy") {
            @Override
            public String getTextToDisplay() {
                return CombatSession.getProjectileAccuracy();
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Hits Taken", 0, 600, "Hits Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getHitsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Arrows Taken", 0, 700, "Arrows Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getArrowsTaken());
            }

            @Override
            public boolean isVisible() {
                return showStatus.getValue() && CombatSession.sessionIsRunning();
            }
        };

        new EnhancedHudText("Projectiles Taken", 0, 800, "Projectiles Taken") {
            @Override
            public String getTextToDisplay() {
                return String.valueOf(CombatSession.getProjectilesTaken());
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

    public static void onArrowDamage(EntityArrow entityArrow, Entity entity) {
        //if (runMod.getValue() && entity.getUniqueID() != Minecraft.getMinecraft().thePlayer.getUniqueID() && entityArrow.shootingEntity instanceof EntityPlayer) CombatSession.onHitByArrow((EntityPlayer) entityArrow.shootingEntity);
    }

    // Called when the user left-clicks another entity
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {

    }

    @SubscribeEvent
    public void onArrowShot(ArrowLooseEvent event) {
        if (runMod.getValue() && event.charge > 2) {
            CombatSession.onArrowShot();
        }
    }

    @SubscribeEvent
    public void onStartUsingItem(PlayerUseItemEvent.Start event) {

    }

    @SubscribeEvent
    public void onStopUsingItem(PlayerUseItemEvent.Stop event) {

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
    public void onLivingDeath(LivingDeathEvent event) {
    }

    @SubscribeEvent
    public void onMousePressed(InputEvent.MouseInputEvent event) {
        if (runMod.getValue() && Mouse.getEventButtonState()) {
            if (Mouse.getEventButton() == 0) {
                CombatSession.onLeftClick();
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