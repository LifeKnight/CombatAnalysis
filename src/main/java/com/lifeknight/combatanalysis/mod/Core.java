package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.hud.EnhancedHudText;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.variables.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lifeknight.combatanalysis.gui.hud.EnhancedHudText.textToRender;
import static net.minecraft.util.EnumChatFormatting.*;

@net.minecraftforge.fml.common.Mod(modid = Core.modId, name = Core.modName, version = Core.modVersion, clientSideOnly = true)
public class Core {
    public static final String
            modName = "ModBase",
            modVersion = "1.0",
            modId = "modbase";
    public static final EnumChatFormatting modColor = WHITE;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new LifeKnightThreadFactory());
    public static boolean onHypixel = false;
    public static GuiScreen guiToOpen = null;
    public static final LifeKnightBoolean runMod = new LifeKnightBoolean("Mod", "Main", true);
    public static final LifeKnightBoolean hudTextShadow = new LifeKnightBoolean("HUDTextShadow", "HUD", true);
    public static final LifeKnightCycle defaultSeparator = new LifeKnightCycle("DefaultSeparator", "HUD", new ArrayList<>(Arrays.asList(" > ", ": ", " | ", " - "))) {
        @Override
        public void onValueChange() {
            for (EnhancedHudText enhancedHudText : textToRender) {
                enhancedHudText.setSeparator(this.getValue());
            }
        }
    };
    public static final LifeKnightCycle defaultPrefixColor = new LifeKnightCycle("DefaultPrefixColor", "HUD", new ArrayList<>(Arrays.asList(
            "Red",
            "Gold",
            "Yellow",
            "Green",
            "Aqua",
            "Blue",
            "Light Purple",
            "Dark Red",
            "Dark Green",
            "Dark Aqua",
            "Dark Blue",
            "Dark Purple",
            "White",
            "Gray",
            "Dark Gray",
            "Black"
    ))) {
        @Override
        public void onValueChange() {
            for (EnhancedHudText enhancedHudText : textToRender) {
                enhancedHudText.setPrefixColor(this.getValue());
            }
        }
    };
    public static final LifeKnightCycle defaultContentColor = new LifeKnightCycle("DefaultContentColor", "HUD", new ArrayList<>(Arrays.asList(
            "Red",
            "Gold",
            "Yellow",
            "Green",
            "Aqua",
            "Blue",
            "Light Purple",
            "Dark Red",
            "Dark Green",
            "Dark Aqua",
            "Dark Blue",
            "Dark Purple",
            "White",
            "Gray",
            "Dark Gray",
            "Black"
    ))) {
        @Override
        public void onValueChange() {
            for (EnhancedHudText enhancedHudText : textToRender) {
                enhancedHudText.setContentColor(this.getValue());
            }
        }
    };

        public static Configuration configuration;

        @EventHandler
        public void init(FMLInitializationEvent initEvent) {
            MinecraftForge.EVENT_BUS.register(this);
            ClientCommandHandler.instance.registerCommand(new ModCommand());

            configuration = new Configuration();
        }

        @SubscribeEvent
        public void onConnect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (Minecraft.getMinecraft().theWorld != null) {
                        for (String msg : Chat.queuedMessages) {
                            Chat.addChatMessage(msg);
                        }
                    }
                    onHypixel = !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net");
                }
            }, 2000);
        }

        @SubscribeEvent
        public void onChatMessageReceived(ClientChatReceivedEvent event) {

        }

        @SubscribeEvent
        public void onRenderTick(TickEvent.RenderTickEvent event) {
            if (guiToOpen != null) {
                Minecraft.getMinecraft().displayGuiScreen(guiToOpen);
                guiToOpen = null;
            }

            if (Minecraft.getMinecraft().inGameHasFocus) {
                EnhancedHudText.doRender();
            }
        }

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                EntityLivingBase opponent = Minecraft.getMinecraft().thePlayer.getLastAttacker();

                if (opponent != null) {

                }
            }
        }

        public static void openGui(GuiScreen guiScreen) {
            guiToOpen = guiScreen;
        }
}