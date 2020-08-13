package com.lifeknight.combatanalysis.mod;

import com.google.gson.*;
import com.lifeknight.combatanalysis.utilities.Logic;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class CombatSession {
    private static final List<CombatSession> combatSessions = new ArrayList<>();
    private static int highestId = 0;
    private static boolean sessionIsRunning = false;
    private static int attackTimer = 0;
    private static int attackType;

    public static void readLoggedCombatSessions() {
        for (String log : Core.combatSessionLogger.getLogs()) {
            Scanner scanner = new Scanner(log);
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith("{")) {
                    try {
                        CombatSession combatSession = fromJson(line);
                        highestId = Math.max(highestId, combatSession.id);
                        combatSessions.add(combatSession);
                    } catch (Exception exception) {
                        FMLLog.log(Level.WARN, "An error occurred while attempting to interpret a logged Combat Session: %s", exception.getMessage());
                    }
                }
            }
        }
    }

    private static boolean canStartCombatSession() {
        if (Minecraft.getMinecraft().playerController.getCurrentGameType() != WorldSettings.GameType.SURVIVAL) return false;

        for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (entityPlayer != null && entityPlayer.getUniqueID() != Minecraft.getMinecraft().thePlayer.getUniqueID() &&
                    !entityPlayer.isInvisible() && !entityPlayer.isEntityInvulnerable(DamageSource.causePlayerDamage(Minecraft.getMinecraft().thePlayer))) {
                return true;
            }
        }
        return false;
    }

    public static List<CombatSession> getCombatSessions() {
        return combatSessions;
    }

    public static boolean sessionIsRunning() {
        return sessionIsRunning;
    }

    public static CombatSession createAndActivate() {
        CombatSession combatSession = new CombatSession();
        combatSession.activate();
        combatSessions.add(combatSession);
        return combatSession;
    }

    public static CombatSession getLatestAnalysis() {
        return !sessionIsRunning ? createAndActivate() : combatSessions.get(combatSessions.size() - 1);
    }

    public static CombatSession getLatestAnalysisForGui() {
        List<CombatSession> combatSessions = getCombatSessions();
        return combatSessions.size() == 0 ? null : combatSessions.get(combatSessions.size() - 1);
    }

    public static List<CombatSession> getCombatSessionsForGui() {
        return combatSessions;
    }

    public static void onWorldLoad() {
        if (sessionIsRunning) getLatestAnalysis().end();
    }

    public static void onTick() {
        if (sessionIsRunning) getLatestAnalysis().tick();
        if (attackTimer > 0) {
            attackTimer--;
        } else {
            attackType = Integer.MIN_VALUE;
        }
    }

    public static void onArrowShot() {
        if ((Core.automaticSessions.getValue() && canStartCombatSession()) || sessionIsRunning)
            getLatestAnalysis().arrowShot();
    }

    public static void onArrowHit(EntityPlayer target) {
        if (sessionIsRunning) {
            getLatestAnalysis().arrowHit(target);
        } else {
            attackTimer = 5;
            attackType = 1;
        }
    }

    public static void onHitByArrow(EntityPlayer shooter) {
        if ((Core.automaticSessions.getValue() && canStartCombatSession()) || sessionIsRunning)
            getLatestAnalysis().hitByArrow(shooter);
    }

    public static void onProjectileThrown(int type) {
        if ((Core.automaticSessions.getValue() && canStartCombatSession() && type == 0) || sessionIsRunning)
            getLatestAnalysis().projectileThrown(type);
    }

    public static void onProjectileHit(EntityPlayer target) {
        if (sessionIsRunning) {
            getLatestAnalysis().projectileHit(target);
        } else {
            attackTimer = 5;
            attackType = 2;
        }
    }

    public static void onHitByProjectile(EntityPlayer thrower) {
        if ((Core.automaticSessions.getValue() && canStartCombatSession()) || sessionIsRunning)
            getLatestAnalysis().hitByProjectile(thrower, 1);
    }

    public static void onAttack(EntityPlayer target) {
        if ((Core.automaticSessions.getValue() && canStartCombatSession()) || sessionIsRunning) {
            getLatestAnalysis().attack(target);
        } else {
            attackTimer = 5;
            attackType = 0;
        }
    }

    public static void onPlayerHurt(EntityPlayer player) {
        if (sessionIsRunning) getLatestAnalysis().playerHurt(player);
    }

    public static void onHurt(EntityPlayer attacker) {
        if (sessionIsRunning) getLatestAnalysis().hurt(attacker);
    }

    public static void onLeftClick() {
        if (sessionIsRunning) getLatestAnalysis().leftClick();
    }

    public static void onRightClick() {
        if (sessionIsRunning) getLatestAnalysis().rightClick();
    }

    public static void onKeyTyped(int keyCode, boolean state) {
        if (sessionIsRunning) getLatestAnalysis().keyTyped(keyCode, state);
    }

    public static int leftClicks() {
        return sessionIsRunning ? getLatestAnalysis().leftClicks : 0;
    }

    public static int rightClicks() {
        return sessionIsRunning ? getLatestAnalysis().rightClicks : 0;
    }

    public static int attacksSent() {
        return sessionIsRunning ? getLatestAnalysis().attacksSent : 0;
    }

    public static int attacksLanded() {
        return sessionIsRunning ? getLatestAnalysis().attacksLanded : 0;
    }

    public static String attackAccuracy() {
        return !sessionIsRunning || attacksSent() == 0 ? "N/A" : Text.shortenDouble(attacksLanded() / (double) attacksSent() * 100, 1) + "%";
    }

    public static int projectilesThrown() {
        return sessionIsRunning ? getLatestAnalysis().projectilesThrown : 0;
    }

    public static int projectilesHit() {
        return sessionIsRunning ? getLatestAnalysis().projectilesHit : 0;
    }

    public static String projectileAccuracy() {
        return !sessionIsRunning || projectilesThrown() == 0 ? "N/A" : Text.shortenDouble(projectilesHit() / (double) projectilesThrown() * 100, 1) + "%";
    }

    public static int arrowsShot() {
        return sessionIsRunning ? getLatestAnalysis().arrowsShot : 0;
    }

    public static int arrowsHit() {
        return sessionIsRunning ? getLatestAnalysis().arrowsHit : 0;
    }

    public static String arrowAccuracy() {
        return !sessionIsRunning || arrowsShot() == 0 ? "N/A" : Text.shortenDouble(arrowsHit() / (double) arrowsShot() * 100, 1) + "%";
    }

    public static int hitsTaken() {
        return sessionIsRunning ? getLatestAnalysis().hitsTaken : 0;
    }

    public static int projectilesTaken() {
        return sessionIsRunning ? getLatestAnalysis().projectilesTaken : 0;
    }

    public static int arrowsTaken() {
        return sessionIsRunning ? getLatestAnalysis().arrowsTaken : 0;
    }

    private final int id;
    String version = Core.MOD_VERSION;
    private long startTime;
    private long endTime;
    private boolean deleted = false;
    private final String serverIp;

    private int ticksSinceAction = 0;
    private int lastAttackType;
    private int lastAttackTimer = 0;
    private final Map<UUID, Opponent> opponents;

    private int leftClicks = 0;
    private int rightClicks = 0;

    private int attacksSent = 0;
    private int attacksLanded = 0;
    private boolean criticalHit = false;

    private int projectilesThrown = 0;
    private int projectilesHit = 0;

    private int arrowsShot = 0;
    private int arrowsHit = 0;

    private int hitsTaken = 0;
    private int arrowsTaken = 0;
    private boolean hitByArrow = false;
    private int projectilesTaken = 0;
    private boolean hitByFishingHook = false;

    // More fields such as armor and inventory items and potion effects
    private List<ItemStack> startingInventory;
    private List<ItemStack> startingArmor;

    private List<ItemStack> endingInventory;
    private List<ItemStack> endingArmor;

    private final List<PotionEffectTracker> potionEffects;

    // Strafing data
    private final List<StrafingTracker> strafes;

    // Proper w-tapping/blockhitting/projectile usage

    // Hotkey time
    private int lastHeldItemIndex;
    private final List<HotKeyTracker> hotKeys;

    // Left Clicks Per Second
    private final List<ClicksPerSecondTracker> clicksPerSecondTrackers;
    private int ticksSinceLeftClick = 0;

    public CombatSession() {
        this.id = ++highestId;
        this.serverIp = Minecraft.getMinecraft().getCurrentServerData().serverIP;
        this.opponents = new HashMap<>();
        this.potionEffects = new ArrayList<>();
        this.strafes = new ArrayList<>();
        this.hotKeys = new ArrayList<>();
        this.clicksPerSecondTrackers = new ArrayList<>();
    }

    public CombatSession(int id, String version, long startTime, long endTime, String serverIp, List<Opponent> opponents, int leftClicks, int rightClicks, int attacksSent, int attacksLanded, int projectilesThrown, int projectilesHit, int arrowsShot, int arrowsHit, int hitsTaken, int arrowsTaken, int projectilesTaken, List<ItemStack> startingInventory, List<ItemStack> startingArmor, List<ItemStack> endingInventory, List<ItemStack> endingArmor, List<PotionEffectTracker> potionEffects, List<StrafingTracker> strafes, List<HotKeyTracker> hotKeys, List<ClicksPerSecondTracker> clicksPerSecondTrackers) {
        this.id = id;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serverIp = serverIp;
        this.opponents = new HashMap<>();
        for (Opponent opponent : opponents) {
            this.opponents.put(UUID.randomUUID(), opponent);
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
        this.startingInventory = startingInventory;
        this.startingArmor = startingArmor;
        this.endingInventory = endingInventory;
        this.endingArmor = endingArmor;
        this.potionEffects = potionEffects;
        this.strafes = strafes;
        this.hotKeys = hotKeys;
        this.clicksPerSecondTrackers = clicksPerSecondTrackers;
    }

    public void activate() {
        this.startTime = System.currentTimeMillis();
        sessionIsRunning = true;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        EntityPlayer closestPlayer;
        if ((closestPlayer = this.getClosestPlayer()) != null) {
            this.opponents.put(closestPlayer.getUniqueID(), new Opponent(closestPlayer));
        }

        this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());

        this.startingInventory = Arrays.asList(thePlayer.inventory.mainInventory.clone());
        this.startingArmor = Arrays.asList(thePlayer.inventory.armorInventory.clone());
        this.lastHeldItemIndex =  thePlayer.inventory.currentItem;

        this.endingArmor = Arrays.asList(thePlayer.inventory.armorInventory.clone());
        this.endingInventory = Arrays.asList(thePlayer.inventory.mainInventory.clone());
    }

    public void tick() {
        if (this.lastAttackTimer > 0) {
            this.lastAttackTimer--;
        } else {
            this.lastAttackType = Integer.MIN_VALUE;
        }

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null || thePlayer.getDistance(thePlayer.prevPosX, thePlayer.prevPosY, thePlayer.prevPosZ) > 50) {
            this.end();
            return;
        } else if (thePlayer.capabilities.allowFlying || thePlayer.isInvisible()) {
            this.end();
            return;
        } else if (this.allOpponentsAreGone()) {
            this.end();
            return;
        }

        for (Opponent opponent : this.opponents.values()) {
            opponent.tick();
        }

        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(1.0D, 1.0D, 1.0D));

        for (Entity entity : closestEntities) {
            if (!this.hitByArrow) {
                if (entity instanceof EntityArrow && ((EntityArrow) entity).shootingEntity.getUniqueID() != thePlayer.getUniqueID()) {
                    this.hitByArrow = true;
                }
            }
            this.hitByFishingHook = entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != thePlayer.getUniqueID();
        }

        if (this.lastHeldItemIndex != thePlayer.inventory.currentItem) {
            this.heldItemChange();
        }
        this.lastHeldItemIndex = thePlayer.inventory.currentItem;

        this.potionEffectChange();

        if (Logic.isWithinRange(this.getNonNullElementCount(this.endingArmor), this.getNonNullElementCount(thePlayer.inventory.armorInventory), 1)) {
            this.endingArmor = Arrays.asList(thePlayer.inventory.armorInventory);
        }
        if (Logic.isWithinRange(this.getNonNullElementCount(this.endingInventory), this.getNonNullElementCount(thePlayer.inventory.mainInventory), 3)) {
            this.endingInventory = Arrays.asList(thePlayer.inventory.mainInventory);
        }

        if (this.ticksSinceLeftClick >= 10) {
            ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
            if (clicksPerSecondTracker.getClicksPerSecond() >= 6 && !clicksPerSecondTracker.hasEnded()) {
                clicksPerSecondTracker.end();
                this.clicksPerSecondTrackers.add(new ClicksPerSecondTracker());
            } else if (clicksPerSecondTracker.getLeftClicks() != 0) {
                clicksPerSecondTracker.resetClicks();
            }
        }

        this.ticksSinceAction++;
        this.ticksSinceLeftClick++;
    }

    private boolean allOpponentsAreGone() {
        for (Opponent opponent : this.opponents.values()) {
            if (!(opponent.opponent.isInvisible() || opponent.opponent.isDead)) {
                return false;
            }
        }
        return true;
    }

    private int getNonNullElementCount(List<?> objects) {
        int nonNullCount = 0;
        for (Object object : objects) {
            if (object != null) nonNullCount++;
        }
        return nonNullCount;
    }

    private int getNonNullElementCount(Object[] objects) {
        int nonNullCount = 0;
        for (Object object : objects) {
            if (object != null) nonNullCount++;
        }
        return nonNullCount;
    }

    private void heldItemChange() {
        int currentItem = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
        HotKeyTracker hotKeyTracker;
        if (this.hotKeys.size() == 0) {
            if (currentItem != Core.mainHotBarSlot.getValue() - 1) {
                this.hotKeys.add(new HotKeyTracker());
            }
        }
        if (this.hotKeys.size() != 0) {
            if (!(hotKeyTracker = this.getLatestHotKeyTracker()).hasEnded() && currentItem == Core.mainHotBarSlot.getValue() - 1) {
                hotKeyTracker.end();
            }
            if (this.getLatestHotKeyTracker().hasEnded() && currentItem != Core.mainHotBarSlot.getValue() - 1) {
                this.hotKeys.add(new HotKeyTracker());
            }
        }
    }

    private HotKeyTracker getLatestHotKeyTracker() {
        return this.hotKeys.get(this.hotKeys.size() - 1);
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

    public void updateTicksSinceAction() {
        this.ticksSinceAction = 0;
    }

    private Opponent getOpponent(EntityPlayer opponent) {
        if (opponent == null) opponent = this.getClosestPlayer();
        Opponent theOpponent;
        if ((theOpponent = this.opponents.get(opponent.getUniqueID())) != null) return theOpponent;

        this.opponents.put(opponent.getUniqueID(), (theOpponent = new Opponent(opponent)));
        return theOpponent;
    }

    public void arrowShot() {
        this.updateTicksSinceAction();
        if (this.rightClicks == 0) this.rightClicks = 1;
        this.arrowsShot++;
    }

    public void arrowHit(EntityPlayer target) {
        this.updateTicksSinceAction();
        this.lastAttackType = 1;
        this.lastAttackTimer = 5;
    }

    public void hitByArrow(EntityPlayer shooter) {
        if (shooter == null) return;
        this.updateTicksSinceAction();
        this.hitByArrow = false;
        this.arrowsTaken++;
        this.getOpponent(shooter).arrowsTaken++;
    }

    public void projectileThrown(int type) {
        this.updateTicksSinceAction();
        if (this.rightClicks == 0) {
            this.rightClicks = 1;
        }
        this.projectilesThrown++;
    }

    public void projectileHit(EntityPlayer target) {
        this.updateTicksSinceAction();
        this.lastAttackType = 2;
        this.lastAttackTimer = 5;
    }

    public void hitByProjectile(EntityPlayer thrower, int source) {
        if (thrower == null) return;
        this.updateTicksSinceAction();
        this.hitByFishingHook = false;
        if (source == 1 && thrower.fishEntity != null) return;
        this.projectilesTaken++;
        this.getOpponent(thrower).projectilesTaken++;
    }

    public void attack(EntityPlayer target) {
        this.updateTicksSinceAction();
        if (this.attacksSent == 0) {
            this.attacksSent = 1;
            this.leftClicks = 1;
        }
        this.lastAttackType = 0;
        this.lastAttackTimer = 5;
        this.getOpponent(target).attacksLanded++;
        this.attacksLanded++;
        this.criticalHit = Minecraft.getMinecraft().thePlayer.motionY < 0;
    }

    public void playerHurt(EntityPlayer player) {
        this.updateTicksSinceAction();
        if (this.getOpponent(player).opponentHitByFishingHook) {
            this.getOpponent(player).projectilesHit++;
            this.projectilesHit++;
            return;
        }

        if (this.lastAttackTimer > 0) {
            switch (this.lastAttackType) {
                case 0:
                    this.getOpponent(player).onOpponentHit(this.criticalHit);
                    break;
                case 1:
                    this.getOpponent(player).arrowsHit++;
                    this.arrowsHit++;
                    break;
                case 2:
                    this.getOpponent(player).projectilesHit++;
                    this.projectilesHit++;
                    break;
            }
        }
    }

    public void hurt(EntityPlayer attacker) {
        this.updateTicksSinceAction();
        if (this.hitByArrow) {
            this.hitByArrow(attacker);
        } else if (this.hitByFishingHook) {
            this.hitByProjectile(attacker, 0);
        } else {
            this.hitsTaken++;
            this.getOpponent(attacker).onDamage();
        }
    }

    public void leftClick() {
        this.updateTicksSinceAction();
        this.leftClicks++;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        EntityPlayer closestPlayer = this.getClosestPlayer();
        if (thePlayer != null && closestPlayer != null) {
            if (thePlayer.getDistanceToEntity(closestPlayer) <= 3.01) {
                this.attacksSent++;
                this.getOpponent(closestPlayer).attacksSent++;
            }
            if (thePlayer.getDistanceToEntity(closestPlayer) <= 6) {
                this.ticksSinceLeftClick = 0;
                this.getLatestClicksPerSecondCounter().incrementClicks();
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
            if (thePlayer.getUniqueID() != entityPlayer.getUniqueID() && (closestPlayer == null ||
                    (thePlayer.getDistanceToEntity(entityPlayer) < thePlayer.getDistanceToEntity(closestPlayer)
                            && !entityPlayer.isInvisible()))) {
                closestPlayer = entityPlayer;
            }
        }
        return closestPlayer;
    }

    public void rightClick() {
        this.updateTicksSinceAction();
        this.rightClicks++;
    }

    public void keyTyped(int keyCode, boolean state) {
        if (!(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode() == keyCode ||
                Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode() == keyCode)) return;

        boolean leftIsDown = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode());
        boolean rightIsDown = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode());

        if (leftIsDown && !rightIsDown) {
            this.checkAndEndAddStrafing(false);
        } else if (!leftIsDown && rightIsDown) {
            this.checkAndEndAddStrafing(true);
        } else if (this.strafes.size() != 0 && !this.getLatestStrafingTracker().hasEnded()) {
            this.getLatestStrafingTracker().end();
        }
    }

    private void checkAndEndAddStrafing(boolean isRightStrafe) {
        StrafingTracker strafingTracker;
        if (this.strafes.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded())
            strafingTracker.end();
        this.strafes.add(new StrafingTracker(isRightStrafe));
    }

    private StrafingTracker getLatestStrafingTracker() {
        return this.strafes.get(this.strafes.size() - 1);
    }

    public void end() {
        this.endTime = System.currentTimeMillis();
        sessionIsRunning = false;

        for (Opponent opponent : this.opponents.values()) {
            opponent.end();
        }

        HotKeyTracker hotKeyTracker;
        if (this.hotKeys.size() != 0 && !(hotKeyTracker = this.hotKeys.get(this.hotKeys.size() - 1)).hasEnded()) {
            hotKeyTracker.end();
        }


        for (PotionEffectTracker potionEffectTracker : this.potionEffects) {
            if (!potionEffectTracker.hasEnded()) potionEffectTracker.end();
        }

        StrafingTracker strafingTracker;
        if (this.strafes.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded()) {
            strafingTracker.end();
        }

        ClicksPerSecondTracker clicksPerSecondTracker = this.getLatestClicksPerSecondCounter();
        if ((clicksPerSecondTracker.getClicksPerSecond() >= 6 && !clicksPerSecondTracker.hasEnded()) || this.clicksPerSecondTrackers.size() == 0) {
            clicksPerSecondTracker.end();
        }

        if (Core.logSessions.getValue()) {
            Core.combatSessionLogger.plainLog(this.toString());
        }
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

    public int getAttacksSent() {
        return this.attacksSent;
    }

    public int getAttacksLanded() {
        return this.attacksLanded;
    }

    public int getProjectilesThrown() {
        return this.projectilesThrown;
    }

    public int getProjectilesHit() {
        return this.projectilesHit;
    }

    public int getArrowsShot() {
        return this.arrowsShot;
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
        if (this.clicksPerSecondTrackers.size() == 0 || (this.clicksPerSecondTrackers.size() == 1 && !this.getLatestClicksPerSecondCounter().hasEnded()))
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

    public String getAttackAccuracy() {
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

    public long getEndTime() {
        return this.endTime;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public Collection<Opponent> getOpponents() {
        return this.opponents.values();
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

    public List<StrafingTracker> getStrafes() {
        return this.strafes;
    }

    public List<HotKeyTracker> getHotKeys() {
        return this.hotKeys;
    }

    public long getTime() {
        return this.endTime - this.startTime;
    }

    @Override
    public String toString() {
        JsonObject asJsonObject = new JsonObject();
        asJsonObject.addProperty("id", this.id);
        asJsonObject.addProperty("version", this.version);
        asJsonObject.addProperty("startTime", this.startTime);
        asJsonObject.addProperty("endTime", this.endTime);
        asJsonObject.addProperty("serverIp", this.serverIp);
        asJsonObject.add("opponents",  Miscellaneous.toJsonArrayString(new ArrayList<>(this.opponents.values())));
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
        asJsonObject.add("startingInventory", itemStackArrayToJsonArray(this.startingInventory));
        asJsonObject.add("startingArmor", itemStackArrayToJsonArray(this.startingArmor));
        asJsonObject.add("endingInventory", itemStackArrayToJsonArray(this.endingInventory));
        asJsonObject.add("endingArmor", itemStackArrayToJsonArray(this.endingArmor));
        asJsonObject.add("potionEffects", Miscellaneous.toJsonArrayString(this.potionEffects));
        asJsonObject.add("strafes", Miscellaneous.toJsonArrayString(this.strafes));
        asJsonObject.add("hotKeys", Miscellaneous.toJsonArrayString(this.hotKeys));
        asJsonObject.add("clicksPerSecondTrackers", Miscellaneous.toJsonArrayString(this.clicksPerSecondTrackers));

        return asJsonObject.toString();
    }
    
    private static JsonArray itemStackArrayToJsonArray(List<ItemStack> itemStacks) {
        JsonArray asJsonArray = new JsonArray();
        
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                asJsonArray.add(new JsonPrimitive(itemStack.serializeNBT().toString()));
            }
        }
        return asJsonArray;
    }

    public static CombatSession fromJson(String json) throws Exception {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String version = jsonObject.get("version").getAsString();
        long startTime = jsonObject.get("startTime").getAsLong();
        long endTime = jsonObject.get("endTime").getAsLong();
        String serverIp = jsonObject.get("serverIp").getAsString();
        List<Opponent> opponents = jsonArrayToOpponentList(jsonObject.get("opponents").getAsJsonArray());
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
        List<ItemStack> startingInventory = jsonArrayToItemStackArray(jsonObject.get("startingInventory").getAsJsonArray());
        List<ItemStack> startingArmor = jsonArrayToItemStackArray(jsonObject.get("startingArmor").getAsJsonArray());
        List<ItemStack> endingInventory = jsonArrayToItemStackArray(jsonObject.get("endingInventory").getAsJsonArray());
        List<ItemStack> endingArmor = jsonArrayToItemStackArray(jsonObject.get("endingArmor").getAsJsonArray());
        List<PotionEffectTracker> potionEffects = jsonArrayToPotionEffectTrackerList(jsonObject.get("potionEffects").getAsJsonArray());
        List<StrafingTracker> strafes = jsonArrayToStrafingTrackerList(jsonObject.get("strafes").getAsJsonArray());
        List<HotKeyTracker> hotKeys = jsonArrayToHotKeyTrackerList(jsonObject.get("hotKeys").getAsJsonArray());
        List<ClicksPerSecondTracker> clicksPerSecondTrackers = jsonArrayToClicksPerSecondTrackerList(jsonObject.get("clicksPerSecondTrackers").getAsJsonArray());

        return new CombatSession(id, version, startTime, endTime, serverIp, opponents, leftClicks, rightClicks,
                attacksSent, attacksLanded, projectilesThrown, projectilesHit, arrowsShot, arrowsHit, hitsTaken, arrowsTaken, projectilesTaken,
                startingInventory, startingArmor, endingInventory, endingArmor, potionEffects, strafes, hotKeys, clicksPerSecondTrackers);
    }
    
    private static List<Opponent> jsonArrayToOpponentList(JsonArray jsonArray) {
        List<Opponent> opponents = new ArrayList<>();
        
        for (JsonElement jsonElement : jsonArray) {
            opponents.add(Opponent.fromJson(jsonElement.getAsJsonObject()));
        }
        
        return opponents;
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
    
    private static List<ItemStack> jsonArrayToItemStackArray(JsonArray jsonArray) throws NBTException {
        List<ItemStack> itemStacks = new ArrayList<>();
        
        for (JsonElement jsonElement : jsonArray) {
            itemStacks.add(ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(jsonElement.getAsString())));
        }

        return itemStacks;
    }

    public static class Opponent {
        private final EntityPlayer opponent;
        private final String name;
        private int attacksSent = 0;
        private int attacksLanded = 0;
        private int criticalAttacksLanded = 0;
        private int opponentAttacksTaken = 0;

        private final List<ComboTracker> combos;

        private int hitsTaken = 0;
        private int arrowsTaken = 0;
        private int projectilesTaken = 0;

        private int arrowsHit = 0;
        private int projectilesHit = 0;

        private boolean opponentHitByFishingHook = false;

        public Opponent(EntityPlayer opponent) {
            this.opponent = opponent;
            this.name = opponent.getName();
            this.combos = new ArrayList<>();
            this.combos.add(new ComboTracker());
        }

        public Opponent(String name, int attacksSent, int attacksLanded, int criticalAttacksLanded, int opponentAttacksTaken, List<ComboTracker> combos, int hitsTaken, int arrowsTaken, int projectilesTaken, int arrowsHit, int projectilesHit) {
            this.opponent = null;
            this.name = name;
            this.attacksSent = attacksSent;
            this.attacksLanded = attacksLanded;
            this.criticalAttacksLanded = criticalAttacksLanded;
            this.opponentAttacksTaken = opponentAttacksTaken;
            this.combos = combos;
            this.hitsTaken = hitsTaken;
            this.arrowsTaken = arrowsTaken;
            this.projectilesTaken = projectilesTaken;
            this.arrowsHit = arrowsHit;
            this.projectilesHit = projectilesHit;
        }

        public void tick() {
            List<Entity> closestOpponentEntities = Minecraft.getMinecraft().theWorld.
                    getEntitiesWithinAABBExcludingEntity(
                            this.opponent,
                            this.opponent.getEntityBoundingBox().addCoord(this.opponent.motionX, this.opponent.motionY, this.opponent.motionZ).expand(1.0D, 1.0D, 1.0D));

            for (Entity entity : closestOpponentEntities) {
                this.opponentHitByFishingHook = entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != opponent.getUniqueID();
            }
        }

        private void onOpponentHit(boolean criticalHit) {
            this.opponentAttacksTaken++;
            if (criticalHit) this.criticalAttacksLanded++;
            this.getLatestComboTracker().incrementComboCount();
        }

        private void onDamage() {
            this.hitsTaken++;
            if (this.getLatestComboTracker().getComboCount() >= 3) {
                this.getLatestComboTracker().end();
                this.combos.add(new ComboTracker());
            } else {
                this.getLatestComboTracker().resetComboCount();
            }
        }

        private ComboTracker getLatestComboTracker() {
            return this.combos.get(this.combos.size() - 1);
        }

        public void end() {
            ComboTracker comboTracker;
            if (!(comboTracker = this.getLatestComboTracker()).hasEnded()) {
                comboTracker.end();
            }
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

        public int getAttacksSent() {
            return this.attacksSent;
        }

        public int getAttacksLanded() {
            return this.attacksLanded;
        }

        public int getCriticalAttacksLanded() {
            return this.criticalAttacksLanded;
        }

        public int getOpponentAttacksTaken() {
            return this.opponentAttacksTaken;
        }

        public List<ComboTracker> getCombos() {
            return this.combos;
        }

        public int getArrowsHit() {
            return this.arrowsHit;
        }

        public int getProjectilesHit() {
            return this.projectilesHit;
        }

        @Override
        public String toString() {
            JsonObject asJsonObject = new JsonObject();
            asJsonObject.addProperty("name", this.name);
            asJsonObject.addProperty("attacksSent", this.attacksSent);
            asJsonObject.addProperty("attacksLanded", this.attacksLanded);
            asJsonObject.addProperty("criticalAttacksLanded", this.criticalAttacksLanded);
            asJsonObject.addProperty("opponentAttacksTaken", this.opponentAttacksTaken);
            asJsonObject.add("combos", Miscellaneous.toJsonArrayString(combos));
            asJsonObject.addProperty("hitsTaken", this.hitsTaken);
            asJsonObject.addProperty("arrowsTaken", this.arrowsTaken);
            asJsonObject.addProperty("projectilesTaken", this.projectilesTaken);
            asJsonObject.addProperty("arrowsHit", this.arrowsHit);
            asJsonObject.addProperty("projectilesHit", this.projectilesHit);
            return asJsonObject.toString();
        }

        public static Opponent fromJson(JsonObject jsonObject) {
            String name = jsonObject.get("name").getAsString();
            int attacksSent = jsonObject.get("attacksSent").getAsInt();
            int attacksLanded = jsonObject.get("attacksLanded").getAsInt();
            int criticalAttacksLanded = jsonObject.get("criticalAttacksLanded").getAsInt();
            int opponentAttacksTaken = jsonObject.get("opponentAttacksTaken").getAsInt();
            List<ComboTracker> combos = combosToList(jsonObject.get("combos").getAsJsonArray());
            int hitsTaken = jsonObject.get("hitsTaken").getAsInt();
            int arrowsTaken = jsonObject.get("arrowsTaken").getAsInt();
            int projectilesTaken = jsonObject.get("projectilesTaken").getAsInt();
            int arrowsHit = jsonObject.get("arrowsHit").getAsInt();
            int projectilesHit = jsonObject.get("projectilesHit").getAsInt();

            return new Opponent(name, attacksSent, attacksLanded, criticalAttacksLanded, opponentAttacksTaken, combos, hitsTaken, arrowsTaken, projectilesTaken, arrowsHit, projectilesHit);
        }

        private static List<ComboTracker> combosToList(JsonArray jsonArray) {
            List<ComboTracker> combos = new ArrayList<>();

            for (JsonElement jsonElement : jsonArray) {
                try {
                    combos.add(ComboTracker.fromJson(jsonElement.getAsString()));
                } catch (Exception exception) {

                }
            }
            return combos;
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

        public long getEndTime() {
            return this.endTime;
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

        public long getStartTime() {
            return this.startTime;
        }

        public long getEndTime() {
            return this.endTime;
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
            this.endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return this.endTime != 0;
        }

        public void incrementComboCount() {
            this.comboCount++;
            if (this.comboCount == 1) this.startTime = System.currentTimeMillis();
        }

        public void resetComboCount() {
            this.comboCount = 0;
        }

        public int getComboCount() {
            return this.comboCount;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public long getEndTime() {
            return this.endTime;
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

        public static ComboTracker fromJson(String json) {
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

            long startTime = jsonObject.get("startTime").getAsLong();
            long endTime = jsonObject.get("endTime").getAsLong();
            int comboCount = jsonObject.get("comboCount").getAsInt();

            return new ComboTracker(startTime, endTime, comboCount);
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

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
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
