package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
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
    public static Configuration configuration;

    @EventHandler
    public void init(FMLInitializationEvent initEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ModCommand());

        Miscellaneous.createEnhancedHudTextDefaultPropertyVariables();

        configuration = new Configuration();
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

    public static void onArrowShot() {
        Chat.addChatMessage("Arrow shot.");
    }

    public static void onArrowHit(EntityPlayer target) {
        Chat.addChatMessage("Arrow hit: " + target.getName());
    }

    public static void onHitByArrow(EntityPlayer shooter) {
        Chat.addChatMessage("Hit by arrow: " + shooter.getName());
    }

    public static void onProjectileThrown(int type) {
        switch (type) {
            case 0:
                Chat.addChatMessage("Fishing rod cast.");
                break;
            case 1:
                Chat.addChatMessage("Egg thrown.");
                break;
            default:
                Chat.addChatMessage("Snowball thrown.");
                break;
        }
    }

    public static void onProjectileHit(EntityPlayer target) {
        Chat.addChatMessage("Projectile hit: " + target.getName());
    }

    public static void onHitByProjectile(EntityPlayer thrower) {
        Chat.addChatMessage("Hit by projectile: " + thrower.getName());
    }

    public static void onAttack(EntityPlayer target) {
        Chat.addChatMessage("Entity attacked: " + target.getName());
    }

    public static void onPlayerHurt(EntityPlayer player) {
        Chat.addChatMessage("Entity hurt: " + player.getName());
    }

    public static void onHurt(EntityPlayer attacker) {
        Chat.addChatMessage("Hurt by: " + (attacker == null ? "NULL" : attacker.getName()));
    }

    public static void onLeftClick() {
        //Chat.addChatMessage("Left click.");
    }

    public static void onAttackEntityOtherPlayerMPFrom(EntityOtherPlayerMP entityOtherPlayerMP, DamageSource damageSource) {
        if (damageSource.getEntity() == null || Minecraft.getMinecraft().thePlayer == null) return;
        if (damageSource.getEntity().getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            switch (damageSource.getDamageType()) {
                case "arrow":
                    onArrowHit(entityOtherPlayerMP);
                    break;
                case "thrown":
                    onProjectileHit(entityOtherPlayerMP);
                    break;
            }
        }
    }

    public static void onAttackEntityPlayerSPFrom(DamageSource damageSource) {
        if (!(damageSource.getEntity() instanceof EntityPlayer) || !runMod.getValue()) return;
        if ("thrown".equals(damageSource.getDamageType())) onHitByProjectile((EntityPlayer) damageSource.getEntity());
    }

    public static void onLivingHurt(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityPlayer)) return;
        if (entityLivingBase.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            onHurt(null);
        } else {
            onPlayerHurt((EntityPlayer) entityLivingBase);
        }
    }

    public static void onArrowDamage(EntityArrow entityArrow, Entity entity) {
        if (runMod.getValue() && entity.getUniqueID() != Minecraft.getMinecraft().thePlayer.getUniqueID() && entityArrow.shootingEntity instanceof EntityPlayer) onHitByArrow((EntityPlayer) entityArrow.shootingEntity);
    }

    // Called when the user left-clicks another entity
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (runMod.getValue() && event.target instanceof EntityPlayer) onAttack((EntityPlayer) event.target);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        Chat.addChatMessage("LivingAttack Event");
    }

    @SubscribeEvent
    public void onArrowShot(ArrowLooseEvent event) {
        if (runMod.getValue() && event.charge > 2) {
            onArrowShot();
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
                onProjectileThrown(0);
            } else if (item instanceof ItemEgg) {
                onProjectileThrown(1);
            } else if (item instanceof ItemSnowball) {
                onProjectileThrown(2);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
    }

    @SubscribeEvent
    public void onMousePressed(InputEvent.MouseInputEvent event) {
        if (runMod.getValue() && Mouse.isButtonDown(0) && Mouse.getEventButtonState()) {
            onLeftClick();
        }
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
}