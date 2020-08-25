package com.lifeknight.combatanalysis.mod;

import com.google.common.base.Predicates;
import com.google.gson.*;
import com.lifeknight.combatanalysis.gui.CombatSessionGui;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Logic;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class CombatSession {
    private static final List<CombatSession> combatSessions = new ArrayList<>();
    private static int highestId = 0;
    private static boolean sessionIsRunning = false;
    private static final JsonParser JSON_PARSER = new JsonParser();

    private static CombatSession currentCombatSession;

    // For filter
    public static boolean deletedSessionsOnly = false;
    public static int wonFilterType = 0;
    public static boolean dateFilterType = false;
    public static Date firstDate = new Date(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    public static Date secondDate = new Date(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());

    public static List<String> opponentFilter = new ArrayList<>();
    public static List<String> serverFilter = new ArrayList<>();
    public static List<String> typeFilter = new ArrayList<>();

    private static int ticksSinceLeftClick = 0;

    private static EntityPlayer lastAttackedPlayer = null;
    private static int meleeAttackTimer = 0;
    
    private static int hitByArrowTimer = 0;
    private static int hitByProjectileTimer = 0;

    private static final Map<UUID, EntityThrowable> thrownProjectiles = new HashMap<>();

    public static void getLoggedCombatSessions() {
        List<CombatSession> loggedCombatSessions = new ArrayList<>();
        List<String> logs = Core.combatSessionLogger.getLogs();
        for (String log : logs) {
            if (log != null) {
                Scanner scanner = new Scanner(log);
                String line;
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line.startsWith("{")) {
                        try {
                            CombatSession combatSession = fromJson(line);
                            highestId = Math.max(highestId, combatSession.id);
                            loggedCombatSessions.add(combatSession);
                        } catch (Exception exception) {
                            Miscellaneous.logError("[%d] An error occurred while trying to interpret a combat session from logs: %s, (%s)", logs.indexOf(log), exception.getMessage(), line);
                        }
                    }
                }
                scanner.close();
            }
        }
        if (highestId != 0) highestId++;

        while (loggedCombatSessions.size() != 0) {
            CombatSession nextCombatSession = null;
            for (CombatSession combatSession : loggedCombatSessions) {
                if (nextCombatSession == null || combatSession.startTime < nextCombatSession.startTime) {
                    nextCombatSession = combatSession;
                }
            }
            loggedCombatSessions.remove(nextCombatSession);
            combatSessions.add(nextCombatSession);
        }
    }

    private static boolean canStartCombatSession() {
        if (!userCanStartCombatSession()) return false;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (!(entityPlayer.getUniqueID() == thePlayer.getUniqueID() ||
                    entityPlayer.capabilities.isFlying ||
                    playerIsStationary(entityPlayer))) {
                return true;
            }
        }
        return false;
    }

    private static boolean userCanStartCombatSession() {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        return (Minecraft.getMinecraft().playerController.getCurrentGameType() == WorldSettings.GameType.SURVIVAL &&
                !thePlayer.capabilities.allowFlying);
    }

    public static List<CombatSession> getCombatSessions() {
        return combatSessions;
    }

    public static boolean sessionIsRunning() {
        return sessionIsRunning;
    }

    public static CombatSession createAndActivate() {
        CombatSession combatSession = new CombatSession();
        currentCombatSession = combatSession;
        combatSession.activate();
        return combatSession;
    }

    public static CombatSession getLatestAnalysis() {
        return sessionIsRunning ? currentCombatSession : createAndActivate();
    }

    public static CombatSession getLatestAnalysisForGui() {
        List<CombatSession> combatSessions = getCombatSessionsForGui();
        return combatSessions.isEmpty() ? null : combatSessions.get(combatSessions.size() - 1);
    }

    public static List<CombatSession> getCombatSessionsForGui() {
        List<CombatSession> combatSessions = new ArrayList<>();

        for (CombatSession combatSession : CombatSession.combatSessions) {
            if ((deletedSessionsOnly && combatSession.isDeleted()) || (!deletedSessionsOnly && !combatSession.isDeleted()) &&
                    (wonFilterType == 0 || ((wonFilterType == 1 && combatSession.isWon()) || (wonFilterType == 2 && !combatSession.isWon()))) &&
                    ((dateFilterType && ((firstDate.getTime() == 0 || secondDate.getTime() == 0) || (combatSession.getStartTime() >= firstDate.getTime() && combatSession.getStartTime() <= secondDate.getTime() + 86400000L))) ||
                            (!dateFilterType && (firstDate.getTime() == 0 || (combatSession.getStartTime() >= firstDate.getTime() && combatSession.getStartTime() <= firstDate.getTime() + 86400000L)))) &&
                    (typeFilter.isEmpty() || Text.containsAny(combatSession.detectType(), typeFilter, true)) &&
                    (serverFilter.isEmpty() || Text.containsAny(combatSession.getServerIp(), serverFilter, true)) &&
                    (opponentFilter.isEmpty() || Text.containsAny(opponentFilter, combatSession.getOpponentNames(), true)
                    )) combatSessions.add(combatSession);
        }

        return combatSessions;
    }

    public static void onWorldLoad() {
        if (sessionIsRunning && Core.automaticSessions.getValue() && (Core.allAutoEnd.getValue() || Core.endOnGameEnd.getValue()))
            getLatestAnalysis().end();
    }

    public static void onTick() {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (!(theWorld == null || thePlayer == null)) {
            double d0 = 1.0;
            for (Entity entity : theWorld.getEntities(EntityArrow.class, input -> {
                assert input != null;
                return !input.onGround && input.shootingEntity != null && input.shootingEntity.getUniqueID() != thePlayer.getUniqueID();
            })) {
                EntityArrow entityArrow = (EntityArrow) entity;
                if (theWorld.getEntitiesWithinAABBExcludingEntity(entityArrow,
                        entityArrow.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(thePlayer)) {
                    hitByArrowTimer = 5;
                    break;
                }
            }

            for (Entity entity : theWorld.getEntities(EntityEgg.class, input -> {
                assert input != null;
                return true;
            })) {
                if (theWorld.getEntitiesWithinAABBExcludingEntity(entity,
                        entity.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(thePlayer)) {
                    if (entity.ticksExisted <= 5) {
                        thrownProjectiles.putIfAbsent(entity.getUniqueID(), (EntityThrowable) entity);
                    } else if (!thrownProjectiles.containsKey(entity.getUniqueID())) {
                        hitByProjectileTimer = 5;
                        break;
                    }
                }
            }

            for (Entity entity : theWorld.getEntities(EntitySnowball.class, input -> {
                assert input != null;
                return true;
            })) {
                if (theWorld.getEntitiesWithinAABBExcludingEntity(entity,
                        entity.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(thePlayer)) {
                    if (entity.ticksExisted <= 5) {
                        thrownProjectiles.putIfAbsent(entity.getUniqueID(), (EntityThrowable) entity);
                    } else if (!thrownProjectiles.containsKey(entity.getUniqueID())) {
                        hitByProjectileTimer = 5;
                        break;
                    }
                }
            }

            List<UUID> uuidsToRemove = new ArrayList<>();
            for (UUID uuid : thrownProjectiles.keySet()) {
                if (!theWorld.loadedEntityList.contains(thrownProjectiles.get(uuid)))
                    uuidsToRemove.add(uuid);
            }

            for (UUID uuid : uuidsToRemove) {
                thrownProjectiles.remove(uuid);
            }
        }

        if (sessionIsRunning) getLatestAnalysis().tick();

        if (meleeAttackTimer != 0) meleeAttackTimer--;
        if (hitByArrowTimer != 0) hitByArrowTimer--;
        if (hitByProjectileTimer != 0) hitByProjectileTimer--;
        ticksSinceLeftClick++;
    }

    public static void onArrowShot() {
        if (sessionIsRunning || (Core.automaticSessions.getValue() && canStartCombatSession()))
            getLatestAnalysis().arrowShot();
    }

    public static void onProjectileThrown() {
        if (sessionIsRunning || (Core.automaticSessions.getValue() && canStartCombatSession()))
            getLatestAnalysis().projectileThrown();
    }

    public static void onAttack(EntityPlayer target) {
        lastAttackedPlayer = target;
        meleeAttackTimer = 5;
        if (sessionIsRunning || (Core.automaticSessions.getValue() && canStartCombatSession() && Core.teamedServer))
            getLatestAnalysis().attack(target);

    }

    public static void onPlayerHurt(EntityPlayer player) {
        if (sessionIsRunning || (Core.automaticSessions.getValue() && canStartCombatSession() && lastAttackedPlayer != null && lastAttackedPlayer.getUniqueID() == player.getUniqueID() && meleeAttackTimer != 0))
            getLatestAnalysis().playerHurt(player);
    }

    public static void onHurt(EntityPlayer attacker) {
        if (sessionIsRunning || (Core.automaticSessions.getValue() && canStartCombatSession() && (hitByFishingHook() || hitByArrowTimer != 0 || attackedByPlayer())))
            getLatestAnalysis().hurt(attacker);
    }
    
    private static boolean playerIsStationary(EntityPlayer entityPlayer)  {
        return entityPlayer.lastTickPosX == entityPlayer.posX &&
                entityPlayer.lastTickPosY == entityPlayer.posY &&
                entityPlayer.lastTickPosZ == entityPlayer.posZ;
    }

    private static boolean hitByFishingHook() {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(0.5, 0.5, 0.5));

        for (Entity entity : closestEntities) {
            if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != thePlayer.getUniqueID()) {
                return true;
            }
        }
        return false;
    }

    private static boolean attackedByPlayer() {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(3.5, 3.5, 3.5));

        for (Entity entity : closestEntities) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) entity;
                if (!(entityPlayer.capabilities.isFlying ||
                        (playerIsStationary(entityPlayer)) || entityPlayer.limbSwingAmount == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void onLeftClick() {
        if (sessionIsRunning) getLatestAnalysis().leftClick();
    }

    public static void onRightClick() {
        if (sessionIsRunning) getLatestAnalysis().rightClick();
    }

    public static void onKeyTyped(int keyCode, boolean state) {
        if (state) {
            if (keyCode == Core.toggleCombatSessionKeyBinding.getKeyCode()) {
                if (sessionIsRunning) {
                    getLatestAnalysis().end();
                } else if (Core.runMod.getValue()) {
                    createAndActivate();
                }
            } else if (keyCode == Core.openLatestCombatSessionKeyBinding.getKeyCode()) {
                Core.openGui(new CombatSessionGui(getLatestAnalysisForGui()));
            }
        }
        if (sessionIsRunning) getLatestAnalysis().keyTyped(keyCode);
    }

    public static int attacksSent() {
        return sessionIsRunning ? getLatestAnalysis().attacksSent : 0;
    }

    public static int hitsDealt() {
        return sessionIsRunning ? getLatestAnalysis().getHitsDealt() : 0;
    }

    public static String meleeAccuracy() {
        return !sessionIsRunning || attacksSent() == 0 ? "N/A" : getLatestAnalysis().getMeleeAccuracy();
    }

    public static int projectilesThrown() {
        return sessionIsRunning ? getLatestAnalysis().projectilesThrown : 0;
    }

    public static String projectileAccuracy() {
        return !sessionIsRunning || projectilesThrown() == 0 ? "N/A" : getLatestAnalysis().getProjectileAccuracy();
    }

    public static int arrowsShot() {
        return sessionIsRunning ? getLatestAnalysis().arrowsShot : 0;
    }

    public static int arrowsHit() {
        return sessionIsRunning ? getLatestAnalysis().arrowsHit : 0;
    }

    public static String arrowAccuracy() {
        return !sessionIsRunning || arrowsShot() == 0 ? "N/A" : getLatestAnalysis().getArrowAccuracy();
    }

    public static int hitsTaken() {
        return sessionIsRunning ? getLatestAnalysis().hitsTaken : 0;
    }

    public static int arrowsTaken() {
        return sessionIsRunning ? getLatestAnalysis().arrowsTaken : 0;
    }

    private final int id;
    private String version = Core.MOD_VERSION;
    private boolean logged = false;
    private long startTime;
    private long endTime;
    private boolean deleted = false;
    private final String serverIp;
    private boolean won = false;
    private BlockPos startingPosition;

    private final Map<UUID, OpponentTracker> opponentTrackerMap;

    private int leftClicks = 0;
    private int rightClicks = 0;

    private int attacksSent = 0;
    private int attacksLanded = 0;

    private int projectilesThrown = 0;
    private int projectilesHit = 0;

    private int arrowsShot = 0;
    private int arrowsHit = 0;

    private int hitsTaken = 0;
    private int arrowsTaken = 0;
    private int projectilesTaken = 0;

    private float startingHealth;
    private float endingHealth;

    // More fields such as armor and inventory items and potion effects
    private List<ItemStack> startingInventory;
    private List<ItemStack> startingArmor;

    private List<ItemStack> endingInventory;
    private List<ItemStack> endingArmor;

    private final List<PotionEffectTracker> potionEffects;

    // Strafing data
    private final List<StrafingTracker> strafingTrackers;

    // Hotkey time
    private int lastHeldItemIndex;
    private final List<HotKeyTracker> hotKeyTrackers;

    // Left Clicks Per Second
    private final List<ClicksPerSecondTracker> clicksPerSecondTrackers;

    private String detectedType = null;

    public CombatSession() {
        this.id = highestId;
        this.serverIp = Minecraft.getMinecraft().isSingleplayer() ? "Singleplayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP;
        this.opponentTrackerMap = new HashMap<>();
        this.potionEffects = new ArrayList<>();
        this.strafingTrackers = new ArrayList<>();
        this.hotKeyTrackers = new ArrayList<>();
        this.clicksPerSecondTrackers = new ArrayList<>();
    }

    public CombatSession(int id, String version, long startTime, long endTime, String serverIp, List<OpponentTracker> opponentTrackers, int leftClicks, int rightClicks, int attacksSent, int attacksLanded, int projectilesThrown, int projectilesHit, int arrowsShot, int arrowsHit, int hitsTaken, int arrowsTaken, int projectilesTaken, float startingHealth, float endingHealth, List<ItemStack> startingInventory, List<ItemStack> startingArmor, List<ItemStack> endingInventory, List<ItemStack> endingArmor, List<PotionEffectTracker> potionEffects, List<StrafingTracker> strafes, List<HotKeyTracker> hotKeys, List<ClicksPerSecondTracker> clicksPerSecondTrackers) {
        this.id = id;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serverIp = serverIp;
        this.opponentTrackerMap = new HashMap<>();
        for (OpponentTracker opponentTracker : opponentTrackers) {
            this.opponentTrackerMap.put(UUID.randomUUID(), opponentTracker);
        }
        this.leftClicks = leftClicks;
        this.rightClicks = rightClicks;
        this.attacksSent = attacksSent;
        this.attacksLanded = attacksLanded;
        this.projectilesThrown = projectilesThrown;
        this.projectilesHit = projectilesHit;
        this.arrowsShot = arrowsShot;
        this.arrowsHit = arrowsHit;
        this.hitsTaken = hitsTaken;
        this.arrowsTaken = arrowsTaken;
        this.projectilesTaken = projectilesTaken;
        this.startingHealth = startingHealth;
        this.endingHealth = endingHealth;
        this.startingInventory = startingInventory;
        this.startingArmor = startingArmor;
        this.endingInventory = endingInventory;
        this.endingArmor = endingArmor;
        this.potionEffects = potionEffects;
        this.strafingTrackers = strafes;
        this.hotKeyTrackers = hotKeys;
        this.clicksPerSecondTrackers = clicksPerSecondTrackers;
        this.deleted = Core.deletedSessionIds.getValue().contains(this.id);
        this.won = Core.wonSessionIds.getValue().contains(this.id);
        this.logged = Core.loggedSessionIds.getValue().contains(this.id);
    }

    public void activate() {
        this.startTime = System.currentTimeMillis();
        thrownProjectiles.clear();
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        EntityPlayer closestPlayer;
        if ((closestPlayer = this.getClosestPlayer()) != null && !playerIsStationary(closestPlayer)) {
            this.opponentTrackerMap.put(closestPlayer.getUniqueID(), new OpponentTracker(closestPlayer));
        }

        this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());

        this.startingPosition = thePlayer.getPosition();

        this.startingHealth = thePlayer.getHealth();
        this.endingHealth = thePlayer.getHealth();

        this.startingInventory = Arrays.asList(thePlayer.inventory.mainInventory.clone());
        this.startingArmor = Arrays.asList(thePlayer.inventory.armorInventory.clone());
        this.lastHeldItemIndex = thePlayer.inventory.currentItem;

        this.endingArmor = Arrays.asList(thePlayer.inventory.armorInventory.clone());
        this.endingInventory = Arrays.asList(thePlayer.inventory.mainInventory.clone());

        sessionIsRunning = true;
    }

    public void tick() {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;

        if (thePlayer == null || theWorld == null) {
            if (Core.automaticSessions.getValue() && Core.allAutoEnd.getValue()) {
                this.end();
            }
            return;
        }

        if (Core.automaticSessions.getValue()) {
            if ((Core.allAutoEnd.getValue() || Core.endOnSpectator.getValue()) && thePlayer.capabilities.allowFlying) {
                this.end();
                return;
            } else if (Core.allAutoEnd.getValue() && this.allOpponentsAreGone()) {
                this.end();
                return;
            }
        }

        if (Core.automaticSessions.getValue() && (Core.allAutoEnd.getValue() || Core.endOnGameEnd.getValue())) {
            if (this.startingPosition != null && thePlayer.getDistance(this.startingPosition.getX(), this.startingPosition.getY(), this.startingPosition.getZ()) >= 1500) {
                this.end();
            }
        }

        if (Logic.isWithinRange(this.getNonNullElementCount(this.endingArmor), this.getNonNullElementCount(thePlayer.inventory.armorInventory), 2)) {
            this.endingArmor = Arrays.asList(thePlayer.inventory.armorInventory.clone());
        } else if (Core.automaticSessions.getValue() && (Core.allAutoEnd.getValue() || Core.endOnSpectator.getValue())) {
            this.end();
            return;
        }

        if (Logic.isWithinRange(this.getNonNullElementCount(this.endingInventory), this.getNonNullElementCount(thePlayer.inventory.mainInventory), 3)) {
            this.endingInventory = Arrays.asList(thePlayer.inventory.mainInventory.clone());
        } else if (Core.automaticSessions.getValue() && (Core.allAutoEnd.getValue() || Core.endOnSpectator.getValue())) {
            this.end();
            return;
        }

        for (EntityPlayer entityPlayer : theWorld.playerEntities) {
            if (entityPlayer.getUniqueID() != thePlayer.getUniqueID() && !this.opponentTrackerMap.containsKey(entityPlayer.getUniqueID()) && !playerIsStationary(entityPlayer)) {
                this.opponentTrackerMap.put(entityPlayer.getUniqueID(), new OpponentTracker(entityPlayer));
            }
        }

        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            opponentTracker.tick();
        }

        this.endingHealth = thePlayer.getHealth();

        if (this.lastHeldItemIndex != thePlayer.inventory.currentItem) {
            this.heldItemChange();
        }
        this.lastHeldItemIndex = thePlayer.inventory.currentItem;

        this.potionEffectChange();

        if (ticksSinceLeftClick >= 5) {
            ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
            if (clicksPerSecondTracker.getClicksPerSecond() >= 5 && !clicksPerSecondTracker.hasEnded() && clicksPerSecondTracker.getLeftClicks() != 1) {
                clicksPerSecondTracker.end();
                this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());
            } else if (clicksPerSecondTracker.getLeftClicks() != 0) {
                clicksPerSecondTracker.resetClicks();
            }
        }
    }

    private boolean allOpponentsAreGone() {
        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            if (this.playerHasAppropriateOpponent(opponentTracker.opponent)) {
                if (!(opponentTracker.opponent.isDead) || Minecraft.getMinecraft().theWorld.playerEntities.contains(opponentTracker.opponent)) {
                    return false;
                }
            }
        }
        return true;
    }


    private int getNonNullElementCount(List<?> objects) {
        if (objects == null) return 0;
        int nonNullCount = 0;
        for (Object object : objects) {
            if (object != null) nonNullCount++;
        }
        return nonNullCount;
    }

    private int getNonNullElementCount(Object[] objects) {
        if (objects == null) return 0;
        int nonNullCount = 0;
        for (Object object : objects) {
            if (object != null) nonNullCount++;
        }
        return nonNullCount;
    }

    private void heldItemChange() {
        int currentItem = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
        HotKeyTracker hotKeyTracker;
        if (this.hotKeyTrackers.isEmpty()) {
            if (currentItem != Core.mainHotBarSlot.getValue() - 1) {
                this.hotKeyTrackers.add(new HotKeyTracker());
            }
        }
        if (this.hotKeyTrackers.size() != 0) {
            if (!(hotKeyTracker = this.getLatestHotKeyTracker()).hasEnded()) {
                hotKeyTracker.end();
            }
            if (this.getLatestHotKeyTracker().hasEnded() && currentItem != Core.mainHotBarSlot.getValue() - 1) {
                this.hotKeyTrackers.add(new HotKeyTracker());
            }
        }
        if (currentItem != Core.mainHotBarSlot.getValue() - 1) {
            ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
            if (clicksPerSecondTracker.getClicksPerSecond() >= 5 && !clicksPerSecondTracker.hasEnded() && clicksPerSecondTracker.getLeftClicks() != 1) {
                clicksPerSecondTracker.end();
                this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());
            } else if (clicksPerSecondTracker.getLeftClicks() != 0) {
                clicksPerSecondTracker.resetClicks();
            }
        }
    }

    private HotKeyTracker getLatestHotKeyTracker() {
        return this.hotKeyTrackers.get(this.hotKeyTrackers.size() - 1);
    }

    private void potionEffectChange() {
        Collection<PotionEffect> newPotionEffects = Minecraft.getMinecraft().thePlayer.getActivePotionEffects();
        for (PotionEffectTracker potionEffectTracker : this.potionEffects) {
            if (!potionEffectTracker.hasEnded() && !newPotionEffects.contains(potionEffectTracker.getPotionEffect())) {
                potionEffectTracker.end();
            }
        }

        for (PotionEffect potionEffect : newPotionEffects) {
            if (!this.hasPotionEffectTracker(potionEffect)) {
                this.potionEffects.add(new PotionEffectTracker(potionEffect));
            }
        }
    }

    private boolean hasPotionEffectTracker(PotionEffect potionEffect) {
        for (PotionEffectTracker potionEffectTracker : this.potionEffects) {
            if (!potionEffectTracker.hasEnded() && potionEffectTracker.getPotionEffect().equals(potionEffect)) {
                return true;
            }
        }
        return false;
    }

    private OpponentTracker getOpponent(EntityPlayer opponent) {
        return getOpponent(opponent, false);
    }

    private OpponentTracker getOpponent(EntityPlayer opponent, boolean forced) {
        if (!userCanStartCombatSession()) {
            return null;
        }
        if (opponent == null) opponent = this.getClosestPlayer();
        if (opponent == null) {
            return null;
        }

        if (!(forced || Core.teamedServer || this.playerHasAppropriateOpponent(opponent))) {
            return null;
        }
        return this.opponentTrackerMap.get(opponent.getUniqueID());
    }

    public void arrowShot() {
        if (this.rightClicks == 0) this.rightClicks = 1;
        this.arrowsShot++;
    }

    public void hitByArrow(EntityPlayer shooter) {
        hitByArrowTimer = 0;
        OpponentTracker opponentTracker = this.getOpponent(shooter, true);
        if (opponentTracker == null) return;
        opponentTracker.arrowsTaken++;
        this.arrowsTaken++;
    }

    public void projectileThrown() {
        if (this.rightClicks == 0) {
            this.rightClicks = 1;
        }
        this.projectilesThrown++;
    }

    public void hitByProjectile() {
        hitByProjectileTimer = 0;
        this.projectilesTaken++;
    }

    public void attack(EntityPlayer target) {
        OpponentTracker opponentTracker = this.getOpponent(target);
        if (opponentTracker == null || !this.isWithinRange(target)) {
            return;
        }
        if (this.attacksSent == 0) {
            this.attacksSent = 1;
            this.leftClicks = 1;
            opponentTracker.attacksSent = 1;
        }
        opponentTracker.attacksLanded++;
        this.attacksLanded++;
    }

    private boolean isWithinRange(EntityPlayer entityPlayer) {
        return (entityPlayer.getEntityBoundingBox().expand(3, 3, 3).isVecInside(Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0F)));
    }

    public void playerHurt(EntityPlayer player) {
        OpponentTracker opponentTracker = this.getOpponent(player, true);
        if (opponentTracker == null) {
            return;
        }
        if (opponentTracker.opponentHitByProjectileTimer != 0 || opponentTracker.hitByFishingRodHook()) {
            opponentTracker.opponentHitByProjectileTimer = 0;
            opponentTracker.projectilesHit++;
            this.projectilesHit++;
            return;
        } else if (opponentTracker.opponentHitByArrowTimer != 0) {
            opponentTracker.opponentHitByArrowTimer = 0;
            opponentTracker.arrowsHit++;
            this.arrowsHit++;
            return;
        }
        if (meleeAttackTimer != 0) {
            if (player.getUniqueID() == lastAttackedPlayer.getUniqueID()) {
                opponentTracker.onOpponentHit();
            }
        }
    }

    public void hurt(EntityPlayer attacker) {
        EntityPlayer entityPlayer = hitByFishingRodHook();
        if (hitByArrowTimer != 0) {
            this.hitByArrow(attacker);
        } else if (entityPlayer != null || hitByProjectileTimer != 0) {
            this.hitByProjectile();
        } else {
            EntityPlayer attackingOpponent = this.getAttackingEntityPlayer();
            if (attackingOpponent == null) return;
            OpponentTracker opponentTracker = this.getOpponent(attackingOpponent, true);
            if (opponentTracker == null) return;
            opponentTracker.onUserDamage();
            this.hitsTaken++;
        }
    }

    private EntityPlayer getAttackingEntityPlayer() {
        EntityPlayer closestPlayer = null;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(4.5, 4.5, 4.5));

        for (Entity entity : closestEntities) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) entity;
                if (!(entityPlayer.capabilities.isFlying ||
                        (playerIsStationary(entityPlayer) && !this.playerHasAppropriateOpponent(entityPlayer)) || entityPlayer.limbSwingAmount == 0 || !playerIsAimingAtUser(entityPlayer) || (Core.teamedServer && thePlayer.isOnSameTeam(entityPlayer)))) {
                    closestPlayer = entityPlayer;
                }
            }
        }
        return closestPlayer;
    }

    private boolean playerHasAppropriateOpponent(EntityPlayer entityPlayer) {
        if (Core.teamedServer && this.allOpponentTrackersAreEmpty()) {
            return true;
        }
        OpponentTracker opponentTracker = this.opponentTrackerMap.get(entityPlayer.getUniqueID());
        return !(opponentTracker == null || opponentTracker.isEmpty());
    }

    private boolean allOpponentTrackersAreEmpty() {
        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            if (!opponentTracker.isEmpty()) return false;
        }
        return true;
    }

    private static boolean playerIsAimingAtUser(EntityPlayer entityPlayer) {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        Vec3 vec3 = entityPlayer.getPositionEyes(1.0F);
        Vec3 vec31 = entityPlayer.getLookVec();
        double d0 = 4.25;
        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
        float f = 10F;
        List<Entity> entityPlayerSurroundingEntities = theWorld.getEntitiesInAABBexcluding(entityPlayer, entityPlayer.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity instanceof EntityPlayer));

        for (Entity entityPlayerSurroundingEntity : entityPlayerSurroundingEntities) {
            if (entityPlayerSurroundingEntity.getUniqueID() == Minecraft.getMinecraft().thePlayer.getUniqueID()) {
                float f1 = 2.5F;
                AxisAlignedBB axisAlignedBB = entityPlayerSurroundingEntity.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(vec3, vec32);

                return (movingObjectPosition != null || axisAlignedBB.isVecInside(vec32));
            }
        }
        return false;
    }

    private static EntityPlayer hitByFishingRodHook() {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(0.5, 0.5, 0.5));

        for (Entity entity : closestEntities) {
            if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != thePlayer.getUniqueID()) {
                return ((EntityFishHook) entity).angler;
            }
        }
        return null;
    }

    public void leftClick() {
        this.leftClicks++;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        EntityPlayer closestPlayer = this.getClosestPlayer();
        if (thePlayer == null || closestPlayer == null) {
            return;
        }
        OpponentTracker opponentTracker = this.getOpponent(closestPlayer);
        if (opponentTracker == null) return;
        Vec3 positionEyes = thePlayer.getPositionEyes(1.0F);
        if (closestPlayer.getEntityBoundingBox().expand(6, 6, 6).isVecInside(positionEyes)) {
            ticksSinceLeftClick = 0;
            this.getLatestClicksPerSecondCounter().incrementClicks();
            if (this.isWithinRange(closestPlayer)) {
                this.attacksSent++;
                opponentTracker.attacksSent++;
            }
        } else {
            ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
            if (clicksPerSecondTracker.getClicksPerSecond() >= 5 && !clicksPerSecondTracker.hasEnded() && clicksPerSecondTracker.getLeftClicks() != 1) {
                clicksPerSecondTracker.end();
                this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());
            } else if (clicksPerSecondTracker.getLeftClicks() != 0) {
                clicksPerSecondTracker.resetClicks();
            }
        }
    }

    private ClicksPerSecondTracker getLatestClicksPerSecondCounter() {
        return this.clicksPerSecondTrackers.get(this.clicksPerSecondTrackers.size() - 1);
    }

    private EntityPlayer getClosestPlayer() {
        EntityPlayer closestPlayer = null;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
            if ((closestPlayer == null || thePlayer.getDistanceToEntity(entityPlayer) < thePlayer.getDistanceToEntity(closestPlayer)) && !(entityPlayer.getUniqueID() == thePlayer.getUniqueID() ||
                    entityPlayer.capabilities.isFlying ||
                    (playerIsStationary(entityPlayer) && !this.playerHasAppropriateOpponent(entityPlayer)) || (Core.teamedServer && thePlayer.isOnSameTeam(entityPlayer)))) {
                closestPlayer = entityPlayer;
            }
        }
        return closestPlayer;
    }

    public void rightClick() {
        this.rightClicks++;
    }

    public void keyTyped(int keyCode) {
        if (!(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode() == keyCode ||
                Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode() == keyCode)) return;

        boolean leftIsDown = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode());
        boolean rightIsDown = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode());

        if (leftIsDown && !rightIsDown) {
            this.checkAndEndAddStrafing(false);
        } else if (!leftIsDown && rightIsDown) {
            this.checkAndEndAddStrafing(true);
        } else if (this.strafingTrackers.size() != 0 && !this.getLatestStrafingTracker().hasEnded()) {
            this.getLatestStrafingTracker().end();
        }
    }

    private void checkAndEndAddStrafing(boolean isRightStrafe) {
        StrafingTracker strafingTracker;
        if (this.strafingTrackers.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded())
            strafingTracker.end();
        this.strafingTrackers.add(new StrafingTracker(isRightStrafe));
    }

    private StrafingTracker getLatestStrafingTracker() {
        return this.strafingTrackers.get(this.strafingTrackers.size() - 1);
    }

    public void end() {
        this.endTime = System.currentTimeMillis();
        sessionIsRunning = false;

        List<UUID> opponentUuidsToRemove = new ArrayList<>();
        for (UUID uuid : this.opponentTrackerMap.keySet()) {
            OpponentTracker opponentTracker = this.opponentTrackerMap.get(uuid);
            if (opponentTracker.isEmpty()) opponentUuidsToRemove.add(uuid);
            else opponentTracker.comboTrackers.removeIf(comboTracker -> comboTracker.getComboCount() < 3);
            
        }

        for (UUID uuid : opponentUuidsToRemove) {
            this.opponentTrackerMap.remove(uuid);
        }

        this.hotKeyTrackers.removeIf(hotKeyTracker -> hotKeyTracker.getTime() == 0);

        HotKeyTracker hotKeyTracker;
        if (this.hotKeyTrackers.size() != 0 && !(hotKeyTracker = this.getLatestHotKeyTracker()).hasEnded()) {
            hotKeyTracker.end();
        }

        for (PotionEffectTracker potionEffectTracker : this.potionEffects) {
            if (!potionEffectTracker.hasEnded()) potionEffectTracker.end();
        }

        this.strafingTrackers.removeIf(strafingTracker -> strafingTracker.getTime() == 0);

        StrafingTracker strafingTracker;
        if (this.strafingTrackers.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded()) {
            strafingTracker.end();
        }

        ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
        if ((clicksPerSecondTracker.getClicksPerSecond() >= 5 && !clicksPerSecondTracker.hasEnded() && clicksPerSecondTracker.getLeftClicks() != 1) || this.clicksPerSecondTrackers.isEmpty()) {
            clicksPerSecondTracker.end();
        }
        if (this.opponentTrackerMap.size() > 0 &&
                !(this.hitsTaken == 0 && this.getHitsDealt() == 0 &&
                        this.arrowsTaken == 0 && this.arrowsHit == 0 &&
                        this.projectilesTaken == 0 && this.projectilesHit == 0) && this.getTime() > 1000L) {
            highestId++;
            try {
                this.won = this.mostHitsOnOpponent();
                if (this.won) Core.wonSessionIds.addElement(this.id);
            } catch (IOException ioException) {
                Miscellaneous.logError("Tried to add combat session id to won-id list, action denied: %s", ioException.getMessage());
            }
            combatSessions.add(this);
            if (Core.automaticallyLogSessions.getValue()) this.log();
        }
    }

    public void log() {
        try {
            Core.loggedSessionIds.addElement(this.id);
            Core.combatSessionLogger.plainLog(this.toString());
            this.logged = true;
        } catch (IOException ioException) {
            Miscellaneous.logError("Combat Session winning-id addition to logged-id list action denied: %s", ioException.getMessage());
        }
    }

    private boolean mostHitsOnOpponent() {
        if (this.opponentTrackerMap.isEmpty()) return false;
        List<OpponentTracker> opponentTrackers = new ArrayList<>(this.opponentTrackerMap.values());
        OpponentTracker opponentTracker = opponentTrackers.get(opponentTrackers.size() - 1);
        return opponentTracker.hitsTaken <= opponentTracker.opponentHitsTaken || opponentTracker.hitsTaken <= 3 && opponentTracker.arrowsTaken <= opponentTracker.arrowsHit;
    }

    public int getProjectilesTaken() {
        return this.projectilesTaken;
    }

    public int getLeftClicks() {
        return this.leftClicks;
    }

    public int getRightClicks() {
        return this.rightClicks;
    }

    public int getHitsDealt() {
        int hitsDealt = 0;
        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            hitsDealt += opponentTracker.opponentHitsTaken;
        }
        return hitsDealt;
    }

    public int getArrowsHit() {
        return this.arrowsHit;
    }

    public int getHitsTaken() {
        return this.hitsTaken;
    }

    public int getArrowsTaken() {
        return this.arrowsTaken;
    }

    public String getAverageClicksPerSecond() {
        if (this.clicksPerSecondTrackers.isEmpty() || (this.clicksPerSecondTrackers.size() == 1 && !this.getLatestClicksPerSecondCounter().hasEnded()))
            return "0";

        double totalAverage = 0;
        double trackers = 0;

        for (ClicksPerSecondTracker clicksPerSecondTracker : this.clicksPerSecondTrackers) {
            if (clicksPerSecondTracker.hasEnded()) {
                totalAverage += clicksPerSecondTracker.getClicksPerSecond();
                trackers++;
            }
        }

        return Text.shortenDouble((totalAverage / trackers), 1);
    }

    public String getMeleeAccuracy() {
        return this.attacksSent == 0 ? "N/A" : Text.shortenDouble(this.attacksLanded / (double) this.attacksSent * 100, 1) + "%";
    }

    public String getProjectileAccuracy() {
        return this.projectilesThrown == 0 ? "N/A" : Text.shortenDouble(this.projectilesHit / (double) this.projectilesThrown * 100, 1) + "%";
    }

    public String getArrowAccuracy() {
        return this.arrowsShot == 0 ? "N/A" : Text.shortenDouble(this.arrowsHit / (double) this.arrowsShot * 100, 1) + "%";
    }

    public long getStartTime() {
        return this.startTime;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public Collection<OpponentTracker> getOpponentTrackerMap() {
        return this.opponentTrackerMap.values();
    }

    public Map<ItemStack, Integer> getStartingInventory() {
        Map<ItemStack, Integer> map = new HashMap<>();

        for (ItemStack itemStack : this.startingInventory) {
            if (itemStack != null) {
                Integer stackSize;
                for (ItemStack itemStack1 : map.keySet()) {
                    if (itemStack.getItem().getRegistryName().equals(itemStack1.getItem().getRegistryName())) {
                        itemStack = itemStack1;
                        break;
                    }
                }

                if ((stackSize = map.get(itemStack)) != null) {
                    map.remove(itemStack);
                    map.put(itemStack, stackSize + itemStack.stackSize);
                } else {
                    map.put(itemStack, itemStack.stackSize);
                }
            }
        }
        return map;
    }

    public Map<ItemStack, Integer> getStartingArmor() {
        Map<ItemStack, Integer> map = new HashMap<>();

        for (ItemStack itemStack : this.startingArmor) {
            if (itemStack != null) {
                map.put(itemStack, itemStack.stackSize);
            }
        }
        return map;
    }

    public Map<ItemStack, Integer> getEndingInventory() {
        Map<ItemStack, Integer> map = new HashMap<>();

        for (ItemStack itemStack : this.endingInventory) {
            if (itemStack != null) {
                Integer stackSize;
                for (ItemStack itemStack1 : map.keySet()) {
                    if (itemStack.getItem().getRegistryName().equals(itemStack1.getItem().getRegistryName())) {
                        itemStack = itemStack1;
                        break;
                    }
                }

                if ((stackSize = map.get(itemStack)) != null) {
                    map.remove(itemStack);
                    map.put(itemStack, stackSize + itemStack.stackSize);
                } else {
                    map.put(itemStack, itemStack.stackSize);
                }
            }
        }
        return map;
    }

    public Map<ItemStack, Integer> getEndingArmor() {
        Map<ItemStack, Integer> map = new HashMap<>();

        for (ItemStack itemStack : this.endingArmor) {
            if (itemStack != null) {
                map.put(itemStack, itemStack.stackSize);
            }
        }
        return map;
    }

    public List<PotionEffectTracker> getPotionEffects() {
        return potionEffects;
    }

    public List<StrafingTracker> getStrafingTrackers() {
        return this.strafingTrackers;
    }

    public List<HotKeyTracker> getHotKeyTrackers() {
        return this.hotKeyTrackers;
    }

    public long getTime() {
        return this.endTime - this.startTime;
    }

    public int getId() {
        return this.id;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public boolean isWon() {
        return this.won;
    }

    public float getEndingHealth() {
        return this.endingHealth;
    }

    public float getStartingHealth() {
        return this.startingHealth;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public List<String> getOpponentNames() {
        List<String> names = new ArrayList<>();

        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            names.add(opponentTracker.name);
        }
        return names;
    }

    public String detectType() {
        if (this.detectedType != null) return this.detectedType;

        if (this.getNonNullElementCount(this.startingInventory) <= 2 && this.getNonNullElementCount(this.startingArmor) <= 2)
            this.detectedType = "Sumo";

        Map<String, Integer> map = new HashMap<>();

        for (ItemStack itemStack : this.startingInventory) {
            if (itemStack != null) {
                Integer stackSize;
                if ((stackSize = map.get(itemStack.getItem().getRegistryName())) != null) {
                    map.remove(itemStack.getItem().getRegistryName());
                    map.put(itemStack.getItem().getRegistryName(), stackSize + itemStack.stackSize);
                } else {
                    map.put(itemStack.getItem().getRegistryName(), itemStack.stackSize);
                }
            }
        }
        Integer stackSize;
        if ((stackSize = map.get("minecraft:potion")) != null && stackSize >= 25) this.detectedType = "Potion";

        if ((stackSize = map.get("minecraft:golden_apple")) != null && stackSize >= 3 && stackSize <= 10 && (stackSize = map.get("minecraft:lava_bucket")) != null && stackSize >= 2)
            this.detectedType = "UHC";

        if ((stackSize = map.get("minecraft:golden_apple")) != null && stackSize >= 32) {
            if (this.hitsTaken >= 50 || this.highestHitsOnOpponent() >= 50) return "Combo";
            return "Gapple";
        }

        if (this.detectedType == null) this.detectedType = "Unknown";

        return this.detectedType;
    }

    private int highestHitsOnOpponent() {
        int highestHitsOnOpponent = 0;

        for (OpponentTracker opponentTracker : this.opponentTrackerMap.values()) {
            highestHitsOnOpponent = Math.max(highestHitsOnOpponent, opponentTracker.opponentHitsTaken);
        }
        return highestHitsOnOpponent;
    }

    public boolean isLogged() {
        return this.logged;
    }

    @Override
    public String toString() {
        JsonObject asJsonObject = new JsonObject();

        asJsonObject.addProperty("id", this.id);
        asJsonObject.addProperty("version", this.version);
        asJsonObject.addProperty("startTime", this.startTime);
        asJsonObject.addProperty("endTime", this.endTime);
        asJsonObject.addProperty("serverIp", this.serverIp);
        asJsonObject.add("opponentTrackers", Miscellaneous.toJsonArrayString(new ArrayList<>(this.opponentTrackerMap.values())));
        asJsonObject.addProperty("leftClicks", this.leftClicks);
        asJsonObject.addProperty("rightClicks", this.rightClicks);
        asJsonObject.addProperty("attacksSent", this.attacksSent);
        asJsonObject.addProperty("attacksLanded", this.attacksLanded);
        asJsonObject.addProperty("projectilesThrown", this.projectilesThrown);
        asJsonObject.addProperty("projectilesHit", this.projectilesHit);
        asJsonObject.addProperty("arrowsShot", this.arrowsShot);
        asJsonObject.addProperty("arrowsHit", this.arrowsHit);
        asJsonObject.addProperty("hitsTaken", this.hitsTaken);
        asJsonObject.addProperty("arrowsTaken", this.arrowsTaken);
        asJsonObject.addProperty("projectilesTaken", this.projectilesTaken);
        asJsonObject.addProperty("startingHealth", this.startingHealth);
        asJsonObject.addProperty("endingHealth", this.endingHealth);
        asJsonObject.add("startingInventory", itemStackListToJsonArray(this.startingInventory));
        asJsonObject.add("startingArmor", itemStackListToJsonArray(this.startingArmor));
        asJsonObject.add("endingInventory", itemStackListToJsonArray(this.endingInventory));
        asJsonObject.add("endingArmor", itemStackListToJsonArray(this.endingArmor));
        asJsonObject.add("potionEffects", Miscellaneous.toJsonArrayString(this.potionEffects));
        asJsonObject.add("strafes", Miscellaneous.toJsonArrayString(this.strafingTrackers));
        asJsonObject.add("hotKeys", Miscellaneous.toJsonArrayString(this.hotKeyTrackers));
        asJsonObject.add("clicksPerSecondTrackers", Miscellaneous.toJsonArrayString(this.clicksPerSecondTrackers));

        return asJsonObject.toString();
    }

    private static JsonArray itemStackListToJsonArray(List<ItemStack> itemStacks) {
        JsonArray asJsonArray = new JsonArray();

        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                asJsonArray.add(new JsonPrimitive(itemStack.serializeNBT().toString()));
            }
        }
        return asJsonArray;
    }

    public static CombatSession fromJson(String json) throws Exception {
        JsonObject jsonObject = JSON_PARSER.parse(json).getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String version = jsonObject.get("version").getAsString();
        long startTime = jsonObject.get("startTime").getAsLong();
        long endTime = jsonObject.get("endTime").getAsLong();
        String serverIp = jsonObject.get("serverIp").getAsString();
        List<OpponentTracker> opponentTrackers = jsonArrayToOpponentList(jsonObject.get("opponentTrackers").getAsJsonArray());
        int leftClicks = jsonObject.get("leftClicks").getAsInt();
        int rightClicks = jsonObject.get("rightClicks").getAsInt();
        int attacksSent = jsonObject.get("attacksSent").getAsInt();
        int attacksLanded = jsonObject.get("attacksLanded").getAsInt();
        int projectilesThrown = jsonObject.get("projectilesThrown").getAsInt();
        int projectilesHit = jsonObject.get("projectilesHit").getAsInt();
        int arrowsShot = jsonObject.get("arrowsShot").getAsInt();
        int arrowsHit = jsonObject.get("arrowsHit").getAsInt();
        int hitsTaken = jsonObject.get("hitsTaken").getAsInt();
        int arrowsTaken = jsonObject.get("arrowsTaken").getAsInt();
        int projectilesTaken = jsonObject.get("projectilesTaken").getAsInt();
        float startingHealth = jsonObject.get("startingHealth").getAsFloat();
        float endingHealth = jsonObject.get("endingHealth").getAsFloat();
        List<ItemStack> startingInventory = jsonArrayToItemStackList(jsonObject.get("startingInventory").getAsJsonArray());
        List<ItemStack> startingArmor = jsonArrayToItemStackList(jsonObject.get("startingArmor").getAsJsonArray());
        List<ItemStack> endingInventory = jsonArrayToItemStackList(jsonObject.get("endingInventory").getAsJsonArray());
        List<ItemStack> endingArmor = jsonArrayToItemStackList(jsonObject.get("endingArmor").getAsJsonArray());
        List<PotionEffectTracker> potionEffects = jsonArrayToPotionEffectTrackerList(jsonObject.get("potionEffects").getAsJsonArray());
        List<StrafingTracker> strafes = jsonArrayToStrafingTrackerList(jsonObject.get("strafes").getAsJsonArray());
        List<HotKeyTracker> hotKeys = jsonArrayToHotKeyTrackerList(jsonObject.get("hotKeys").getAsJsonArray());
        List<ClicksPerSecondTracker> clicksPerSecondTrackers = jsonArrayToClicksPerSecondTrackerList(jsonObject.get("clicksPerSecondTrackers").getAsJsonArray());

        return new CombatSession(id, version, startTime, endTime, serverIp, opponentTrackers, leftClicks, rightClicks,
                attacksSent, attacksLanded, projectilesThrown, projectilesHit, arrowsShot, arrowsHit, hitsTaken, arrowsTaken, projectilesTaken, startingHealth, endingHealth,
                startingInventory, startingArmor, endingInventory, endingArmor, potionEffects, strafes, hotKeys, clicksPerSecondTrackers);
    }

    private static List<OpponentTracker> jsonArrayToOpponentList(JsonArray jsonArray) throws NBTException {
        List<OpponentTracker> opponentTrackers = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            opponentTrackers.add(OpponentTracker.fromJson(jsonElement.getAsJsonObject()));
        }

        return opponentTrackers;
    }

    private static List<PotionEffectTracker> jsonArrayToPotionEffectTrackerList(JsonArray jsonArray) throws NBTException {
        List<PotionEffectTracker> potionEffectTrackers = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            potionEffectTrackers.add(PotionEffectTracker.fromJson(jsonElement.getAsJsonObject()));
        }

        return potionEffectTrackers;
    }

    private static List<StrafingTracker> jsonArrayToStrafingTrackerList(JsonArray jsonArray) {
        List<StrafingTracker> strafingTrackers = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            strafingTrackers.add(StrafingTracker.fromJson(jsonElement.getAsJsonObject()));
        }

        return strafingTrackers;
    }

    private static List<HotKeyTracker> jsonArrayToHotKeyTrackerList(JsonArray jsonArray) throws NBTException {
        List<HotKeyTracker> hotKeyTrackers = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            hotKeyTrackers.add(HotKeyTracker.fromJson(jsonElement.getAsJsonObject()));
        }

        return hotKeyTrackers;
    }

    private static List<ClicksPerSecondTracker> jsonArrayToClicksPerSecondTrackerList(JsonArray jsonArray) {
        List<ClicksPerSecondTracker> clicksPerSecondTrackers = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            clicksPerSecondTrackers.add(ClicksPerSecondTracker.fromJson(jsonElement.getAsJsonObject()));
        }

        return clicksPerSecondTrackers;
    }

    private static List<ItemStack> jsonArrayToItemStackList(JsonArray jsonArray) throws NBTException {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            itemStacks.add(ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(jsonElement.getAsString())));
        }

        return itemStacks;
    }

    public static class OpponentTracker {
        private final EntityPlayer opponent;
        private final List<ItemStack> opponentStartingArmor;
        private final String name;
        private int attacksSent = 0;
        private int attacksLanded = 0;
        private int criticalHitsLanded = 0;
        private int opponentHitsTaken = 0;

        private final List<ComboTracker> comboTrackers;

        private int hitsTaken = 0;
        private int criticalHitsTaken = 0;
        private int arrowsTaken = 0;
        private int projectilesTaken = 0;

        private int arrowsHit = 0;
        private int projectilesHit = 0;
        
        private int opponentHitByArrowTimer = 0;
        private int opponentHitByProjectileTimer = 0;

        public OpponentTracker(EntityPlayer opponent) {
            this.opponent = opponent;
            this.opponentStartingArmor = Arrays.asList(opponent.inventory.armorInventory.clone());
            this.name = opponent.getName();
            this.comboTrackers = new ArrayList<>();
            this.comboTrackers.add(new ComboTracker());
        }

        public OpponentTracker(List<ItemStack> opponentStartingArmor, String name, int attacksSent, int attacksLanded, int criticalAttacksLanded, int opponentAttacksTaken, List<ComboTracker> combos, int hitsTaken, int criticalHitsTaken, int arrowsTaken, int projectilesTaken, int arrowsHit, int projectilesHit) {
            this.opponent = null;
            this.opponentStartingArmor = opponentStartingArmor;
            this.name = name;
            this.attacksSent = attacksSent;
            this.attacksLanded = attacksLanded;
            this.criticalHitsLanded = criticalAttacksLanded;
            this.opponentHitsTaken = opponentAttacksTaken;
            this.comboTrackers = combos;
            this.hitsTaken = hitsTaken;
            this.criticalHitsTaken = criticalHitsTaken;
            this.arrowsTaken = arrowsTaken;
            this.projectilesTaken = projectilesTaken;
            this.arrowsHit = arrowsHit;
            this.projectilesHit = projectilesHit;
        }

        public void tick() {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            WorldClient theWorld = Minecraft.getMinecraft().theWorld;
            double d0 = 1.0;
                for (Entity entity : theWorld.getEntities(EntityArrow.class, input -> {
                    assert input != null;
                    return !input.onGround && input.shootingEntity != null && input.shootingEntity.getUniqueID() == thePlayer.getUniqueID(); })) {
                    EntityArrow entityArrow = (EntityArrow) entity;
                    if (theWorld.getEntitiesWithinAABBExcludingEntity(entityArrow,
                            entityArrow.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(this.opponent)) {
                        this.opponentHitByArrowTimer = 5;
                        break;
                    }
                }
                
                for (Entity entity : theWorld.getEntities(EntityEgg.class, input -> {
                    assert input != null;
                    return thrownProjectiles.containsKey(input.getUniqueID()); })) {
                    if (theWorld.getEntitiesWithinAABBExcludingEntity(entity,
                            entity.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(this.opponent)) {
                        this.opponentHitByProjectileTimer = 5;
                        break;
                    }
                }

                for (Entity entity : theWorld.getEntities(EntitySnowball.class, input -> {
                    assert input != null;
                    return thrownProjectiles.containsKey(input.getUniqueID()); })) {
                    if (theWorld.getEntitiesWithinAABBExcludingEntity(entity,
                            entity.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(d0, d0, d0)).contains(this.opponent)) {
                        this.opponentHitByProjectileTimer = 5;
                        break;
                    }
                }

            if (this.opponentHitByArrowTimer != 0) this.opponentHitByArrowTimer--;
            if (this.opponentHitByProjectileTimer != 0) this.opponentHitByProjectileTimer--;
        }

        private void onOpponentHit() {
            this.opponentHitsTaken++;
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer.fallDistance > 0.0F && !thePlayer.onGround && !thePlayer.isOnLadder() && !thePlayer.isInWater() && !thePlayer.isPotionActive(Potion.blindness) && thePlayer.ridingEntity == null)
                this.criticalHitsLanded++;
            this.getLatestComboTracker().incrementComboCount();
        }

        private void onUserDamage() {
            this.hitsTaken++;
            if (this.opponent.fallDistance > 0.0F && !this.opponent.onGround && !this.opponent.isOnLadder() && !this.opponent.isInWater() && !this.opponent.isPotionActive(Potion.blindness) && this.opponent.ridingEntity == null)
                this.criticalHitsTaken++;
            if (this.getLatestComboTracker().getComboCount() >= 3) {
                this.getLatestComboTracker().end();
                this.comboTrackers.add(new ComboTracker());
            } else {
                this.getLatestComboTracker().resetComboCount();
            }
        }

        private boolean isEmpty() {
            if (this.hitsTaken == 0 && this.opponentHitsTaken == 0 &&
                    this.arrowsTaken == 0 && this.arrowsHit == 0 &&
                    this.projectilesTaken == 0 && this.projectilesHit == 0) {
                return true;
            };
            return false;
        }

        private ComboTracker getLatestComboTracker() {
            return this.comboTrackers.get(this.comboTrackers.size() - 1);
        }

        public String getName() {
            return this.name;
        }

        public String attackAccuracy() {
            return this.attacksSent == 0 ? "N/A" : Text.shortenDouble(this.attacksLanded / (double) this.attacksSent * 100, 1) + "%";
        }

        public int getHitsTaken() {
            return this.hitsTaken;
        }

        public int getProjectilesTaken() {
            return this.projectilesTaken;
        }

        public int getArrowsTaken() {
            return this.arrowsTaken;
        }

        public int getCriticalHitsLanded() {
            return this.criticalHitsLanded;
        }

        public int getOpponentHitsTaken() {
            return this.opponentHitsTaken;
        }

        public List<ComboTracker> getComboTrackers() {
            return this.comboTrackers;
        }

        public int getArrowsHit() {
            return this.arrowsHit;
        }

        public int getProjectilesHit() {
            return this.projectilesHit;
        }

        public Map<ItemStack, Integer> getOpponentStartingArmor() {
            Map<ItemStack, Integer> map = new HashMap<>();

            for (ItemStack itemStack : this.opponentStartingArmor) {
                if (itemStack != null) {
                    map.put(itemStack, itemStack.stackSize);
                }
            }
            return map;
        }

        public int getCriticalHitsTaken() {
            return this.criticalHitsTaken;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();

            asJsonObject.add("opponentStartingArmor", CombatSession.itemStackListToJsonArray(this.opponentStartingArmor));
            asJsonObject.addProperty("name", this.name);
            asJsonObject.addProperty("attacksSent", this.attacksSent);
            asJsonObject.addProperty("attacksLanded", this.attacksLanded);
            asJsonObject.addProperty("criticalAttacksLanded", this.criticalHitsLanded);
            asJsonObject.addProperty("opponentAttacksTaken", this.opponentHitsTaken);
            asJsonObject.add("combos", Miscellaneous.toJsonArrayString(this.comboTrackers));
            asJsonObject.addProperty("hitsTaken", this.hitsTaken);
            asJsonObject.addProperty("criticalHitsTaken", this.criticalHitsTaken);
            asJsonObject.addProperty("arrowsTaken", this.arrowsTaken);
            asJsonObject.addProperty("projectilesTaken", this.projectilesTaken);
            asJsonObject.addProperty("arrowsHit", this.arrowsHit);
            asJsonObject.addProperty("projectilesHit", this.projectilesHit);

            return asJsonObject.toString();
        }

        public static OpponentTracker fromJson(JsonObject jsonObject) throws NBTException {
            List<ItemStack> opponentStartingArmor = CombatSession.jsonArrayToItemStackList(jsonObject.get("opponentStartingArmor").getAsJsonArray());
            String name = jsonObject.get("name").getAsString();
            int attacksSent = jsonObject.get("attacksSent").getAsInt();
            int attacksLanded = jsonObject.get("attacksLanded").getAsInt();
            int criticalAttacksLanded = jsonObject.get("criticalAttacksLanded").getAsInt();
            int opponentAttacksTaken = jsonObject.get("opponentAttacksTaken").getAsInt();
            List<ComboTracker> combos = jsonArrayToComboList(jsonObject.get("combos").getAsJsonArray());
            int hitsTaken = jsonObject.get("hitsTaken").getAsInt();
            int criticalHitsTaken = jsonObject.get("criticalHitsTaken").getAsInt();
            int arrowsTaken = jsonObject.get("arrowsTaken").getAsInt();
            int projectilesTaken = jsonObject.get("projectilesTaken").getAsInt();
            int arrowsHit = jsonObject.get("arrowsHit").getAsInt();
            int projectilesHit = jsonObject.get("projectilesHit").getAsInt();

            return new OpponentTracker(opponentStartingArmor, name, attacksSent, attacksLanded, criticalAttacksLanded, opponentAttacksTaken, combos, hitsTaken, criticalHitsTaken, arrowsTaken, projectilesTaken, arrowsHit, projectilesHit);
        }

        private static List<ComboTracker> jsonArrayToComboList(JsonArray jsonArray) {
            List<ComboTracker> combos = new ArrayList<>();

            for (JsonElement jsonElement : jsonArray) {
                ComboTracker comboTracker = ComboTracker.fromJson(jsonElement.getAsJsonObject());
                if (comboTracker.getComboCount() >= 3) combos.add(comboTracker);
            }

            return combos;
        }

        private boolean hitByFishingRodHook() {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                    getEntitiesWithinAABBExcludingEntity(
                            this.opponent,
                            this.opponent.getEntityBoundingBox().addCoord(this.opponent.motionX, this.opponent.motionY, this.opponent.motionZ).expand(0.5, 0.5, 0.5));

            for (Entity entity : closestEntities) {
                if (entity instanceof EntityFishHook) {
                    return ((EntityFishHook) entity).angler.getUniqueID() == thePlayer.getUniqueID();
                }
            }
            return false;
        }
    }

    public static class HotKeyTracker {
        private final long startTime;
        private long endTime = 0;
        private final int index;
        private final ItemStack itemStack;

        public HotKeyTracker() {
            this.index = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            this.itemStack = Minecraft.getMinecraft().thePlayer.getHeldItem();
            this.startTime = System.currentTimeMillis();
        }

        private HotKeyTracker(long startTime, long endTime, int index, ItemStack itemStack) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.index = index;
            this.itemStack = itemStack;
        }

        public void end() {
            this.endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return this.endTime != 0;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public long getTime() {
            return this.endTime - this.startTime;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("startTime", startTime);
            asJsonObject.addProperty("endTime", endTime);
            asJsonObject.addProperty("index", index);
            asJsonObject.addProperty("itemStack", itemStack == null ? "null" : itemStack.serializeNBT().toString());
            return asJsonObject.toString();
        }

        public static HotKeyTracker fromJson(JsonObject jsonObject) throws NBTException {
            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            int index = jsonObject.get("index").getAsInt();
            ItemStack itemStack = null;
            String asJson;
            if (!(asJson = jsonObject.get("itemStack").getAsString()).equals("null")) {
                itemStack = ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(asJson));
            }

            return new HotKeyTracker(startTime, endTime, index, itemStack);
        }
    }

    public static class PotionEffectTracker {
        private final long startTime;
        private long endTime = 0;
        private final PotionEffect potionEffect;
        private final int totalDuration;

        public PotionEffectTracker(PotionEffect potionEffect) {
            this.potionEffect = potionEffect;
            this.startTime = System.currentTimeMillis();
            this.totalDuration = potionEffect.getDuration();
        }

        private PotionEffectTracker(long startTime, long endTime, PotionEffect potionEffect, int totalDuration) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.potionEffect = potionEffect;
            this.totalDuration = totalDuration;
        }

        public void end() {
            this.endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return this.endTime != 0;
        }

        public PotionEffect getPotionEffect() {
            return this.potionEffect;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public int getTotalDuration() {
            return totalDuration;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("startTime", this.startTime);
            asJsonObject.addProperty("endTime", this.endTime);
            asJsonObject.addProperty("potionEffect", this.potionEffect.writeCustomPotionEffectToNBT(new NBTTagCompound()).toString());
            asJsonObject.addProperty("totalDuration", this.totalDuration);
            return asJsonObject.toString();
        }

        public static PotionEffectTracker fromJson(JsonObject jsonObject) throws NBTException {
            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            PotionEffect potionEffect = PotionEffect.readCustomPotionEffectFromNBT(JsonToNBT.getTagFromJson(jsonObject.get("potionEffect").getAsString()));
            int totalDuration = jsonObject.get("totalDuration").getAsInt();

            return new PotionEffectTracker(startTime, endTime, potionEffect, totalDuration);
        }
    }

    public static class StrafingTracker {
        private final long startTime;
        private long endTime = 0;
        private final boolean isRightStrafe;

        public StrafingTracker(boolean isRightStrafe) {
            this.isRightStrafe = isRightStrafe;
            this.startTime = System.currentTimeMillis();
        }

        private StrafingTracker(long startTime, long endTime, boolean isRightStrafe) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.isRightStrafe = isRightStrafe;
        }

        public void end() {
            this.endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return this.endTime != 0;
        }

        public boolean isRightStrafe() {
            return this.isRightStrafe;
        }

        public long getTime() {
            return this.endTime - this.startTime;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("startTime", this.startTime);
            asJsonObject.addProperty("endTime", this.endTime);
            asJsonObject.addProperty("isRightStrafe", this.isRightStrafe);
            return asJsonObject.toString();
        }

        public static StrafingTracker fromJson(JsonObject jsonObject) {
            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            boolean isRightStrafe = jsonObject.get("isRightStrafe").getAsBoolean();

            return new StrafingTracker(startTime, endTime, isRightStrafe);
        }
    }

    public static class ComboTracker {
        private long startTime;
        private long endTime = 0;
        private int comboCount = 0;

        public ComboTracker() {
        }

        private ComboTracker(long startTime, long endTime, int comboCount) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.comboCount = comboCount;
        }

        public void end() {
        }

        public void incrementComboCount() {
            this.comboCount++;
            if (this.comboCount == 1) this.startTime = System.currentTimeMillis();
            this.endTime = System.currentTimeMillis();
        }

        public void resetComboCount() {
            this.comboCount = 0;
        }

        public int getComboCount() {
            return this.comboCount;
        }

        public long getTime() {
            return this.endTime - this.startTime;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("startTime", this.startTime);
            asJsonObject.addProperty("endTime", this.endTime);
            asJsonObject.addProperty("comboCount", this.comboCount);
            return asJsonObject.toString();
        }

        public static ComboTracker fromJson(JsonObject jsonObject) {
            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            int comboCount = jsonObject.get("comboCount").getAsInt();

            return new ComboTracker(startTime, endTime, comboCount);
        }

        public long getStartTime() {
            return this.startTime;
        }
    }

    private static class ClicksPerSecondTracker {
        private long startTime;
        private long endTime = 0;
        private int leftClicks;
        private boolean ended = false;

        public ClicksPerSecondTracker() {
        }

        private ClicksPerSecondTracker(long startTime, long endTime, int leftClicks, boolean ended) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.leftClicks = leftClicks;
            this.ended = ended;
        }

        public void incrementClicks() {
            if (this.leftClicks == 0) this.startTime = System.currentTimeMillis();
            this.leftClicks++;
            this.endTime = System.currentTimeMillis();
        }

        public void resetClicks() {
            this.ended = false;
            this.leftClicks = 0;
        }

        public int getLeftClicks() {
            return this.leftClicks;
        }

        public boolean hasEnded() {
            return this.ended;
        }

        public void end() {
            this.ended = true;
        }

        public double getClicksPerSecond() {
            return this.endTime == 0 ? 0 : 1000 * this.leftClicks / (double) (this.endTime - this.startTime);
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("startTime", this.startTime);
            asJsonObject.addProperty("endTime", this.endTime);
            asJsonObject.addProperty("leftClicks", this.leftClicks);
            asJsonObject.addProperty("ended", this.ended);
            return asJsonObject.toString();
        }

        public static ClicksPerSecondTracker fromJson(JsonObject jsonObject) {
            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            int leftClicks = jsonObject.get("leftClicks").getAsInt();

            return new ClicksPerSecondTracker(startTime, endTime, leftClicks, true);
        }
    }
}
