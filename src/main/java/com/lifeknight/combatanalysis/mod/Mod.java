package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
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

@net.minecraftforge.fml.common.Mod(modid = Mod.modId, name = Mod.modName, version = Mod.modVersion, clientSideOnly = true)
public class Mod {
    public static final String
            modName = "Combat Analysis",
            modVersion = "1.0",
            modId = "combatanalysis";
    public static final EnumChatFormatting modColor = GOLD;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new LifeKnightThreadFactory());
    public static boolean onHypixel = false;
    public static GuiScreen guiToOpen = null;
    public static final LifeKnightBoolean runMod = new LifeKnightBoolean("Mod", "Main", true);
    public static final LifeKnightBoolean gridSnapping = new LifeKnightBoolean("Grid Snapping", "HUD", true);
    public static final LifeKnightBoolean hudTextShadow = new LifeKnightBoolean("HUD Text Shadow", "HUD", true);
    public static Configuration configuration;

    /*
    Method for when an entity is hurt (multiplayer): EntityLivingBase's handleStatusUpdate
    Non-arrow damage-causing projectiles in DamageSource line 73; inject with void method to invoke onProjectileHit
    */

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

    public static void onBowShot() {

    }

    public static void onBowHit() {

    }

    public static void onProjectileThrown(int type) {

    }

    public static void onProjectileHit() {

    }

    public static void onAttack(EntityPlayer target) {

    }

    public static void onLeftClick() {

    }

    // Inject into DamageSource line 74
    public static void onThrownDamage(Entity thrower, Entity target) {
        if (runMod.getValue() && thrower.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
            onProjectileHit();
        }
    }

    // Called when the user left-clicks another entity
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (runMod.getValue() && event.target instanceof EntityPlayer) onAttack((EntityPlayer) event.target);
    }

    @SubscribeEvent
    public void onArrowShot(ArrowLooseEvent event) {
        if (runMod.getValue() && event.charge > 2) {
            onBowShot();
        }
    }

    @SubscribeEvent
    public void onStartUsingItem(PlayerUseItemEvent.Start event) {

    }

    @SubscribeEvent
    public void onStopUsingItem(PlayerUseItemEvent.Stop event) {

    }

    @SubscribeEvent
    public void onSoundPlay(PlaySoundAtEntityEvent event) {
        if (runMod.getValue() && event.name.equals("random.bowhit")) {
            onBowHit();
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
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