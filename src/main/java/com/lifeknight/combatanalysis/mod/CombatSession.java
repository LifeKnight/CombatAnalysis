package com.lifeknight.combatanalysis.mod;

import com.google.gson.JsonObject;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Logic;
import com.lifeknight.combatanalysis.utilities.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
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

                    } catch (Exception exception) {
                        FMLLog.log(Level.WARN, exception.getMessage());
                    }
                }
            }
        }
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
        if (Core.automaticSessions.getValue() || sessionIsRunning) getLatestAnalysis().arrowShot();
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
        if (Core.automaticSessions.getValue() || sessionIsRunning) getLatestAnalysis().hitByArrow(shooter);
    }

    public static void onProjectileThrown(int type) {
        if ((Core.automaticSessions.getValue() && type == 0) || sessionIsRunning)
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
        if (Core.automaticSessions.getValue() || sessionIsRunning) getLatestAnalysis().hitByProjectile(thrower, 1);
    }

    public static void onAttack(EntityPlayer target) {
        if (Core.automaticSessions.getValue() || sessionIsRunning) {
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

    private final int id = ++highestId;
    private long startTime;
    private long endTime;
    private final String serverIp = Minecraft.getMinecraft().getCurrentServerData().serverIP;

    private int ticksSinceAction = 0;
    private int lastAttackType;
    private int lastAttackTimer = 0;
    private final Map<UUID, Opponent> opponents = new HashMap<>();

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
    private ItemStack[] startingInventory;
    private ItemStack[] startingArmor;

    private ItemStack[] endingInventory;
    private ItemStack[] endingArmor;

    private Collection<PotionEffect> previousPotionEffects;
    private final List<PotionEffectTracker> potionEffects = new ArrayList<>();

    // Strafing data
    private final List<StrafingTracker> strafes = new ArrayList<>();

    // Proper w-tapping/blockhitting/projectile usage

    // Hotkey time
    private int lastHeldItemIndex;
    private final List<HotKeyTracker> hotKeys = new ArrayList<>();

    public CombatSession() {

    }

    public void activate() {
        this.startTime = System.currentTimeMillis();
        sessionIsRunning = true;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        EntityPlayer closestPlayer;
        if ((closestPlayer = this.getClosestPlayer()) != null) {
            this.opponents.put(closestPlayer.getUniqueID(), new Opponent(closestPlayer));
        }

        this.startingInventory = thePlayer.inventory.mainInventory.clone();
        this.startingArmor = thePlayer.inventory.armorInventory.clone();
        this.lastHeldItemIndex = thePlayer.inventory.currentItem;

        this.endingArmor = thePlayer.inventory.armorInventory.clone();
        this.endingInventory = thePlayer.inventory.mainInventory.clone();

        this.previousPotionEffects = thePlayer.getActivePotionEffects();
    }

    public void tick() {
        this.ticksSinceAction++;

        if (this.lastAttackTimer > 0) {
            this.lastAttackTimer--;
        } else {
            attackType = Integer.MIN_VALUE;
        }

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) {
            end();
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

        if (thePlayer.getDistance(thePlayer.prevPosX, thePlayer.prevPosY, thePlayer.prevPosZ) > 50) {
            end();
            return;
        } else if (thePlayer.capabilities.allowFlying || thePlayer.isInvisible()) {
            end();
            return;
        } else if (allOpponentsAreGone()) {
            end();
            return;
        }

        if (this.lastHeldItemIndex != thePlayer.inventory.currentItem) {
            this.heldItemChange();
        }
        this.lastHeldItemIndex = thePlayer.inventory.currentItem;

        this.potionEffectChange();

        if (Logic.isWithinRange(getNonNullElementCount(this.endingArmor), getNonNullElementCount(thePlayer.inventory.armorInventory), 1)) {
            this.endingArmor = thePlayer.inventory.armorInventory.clone();
        }
        if (Logic.isWithinRange(getNonNullElementCount(this.endingInventory), getNonNullElementCount(thePlayer.inventory.mainInventory), 3)) {
            this.endingInventory = thePlayer.inventory.mainInventory.clone();
        }
    }

    private boolean allOpponentsAreGone() {
        for (Opponent opponent : opponents.values()) {
            if (!(opponent.opponent == null || opponent.opponent.isInvisible() || opponent.opponent.isDead)) {
                return false;
            }
        }
        return true;
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
        } else if (getLatestHotKeyTracker().hasEnded() && currentItem != Core.mainHotBarSlot.getValue() - 1) {
            this.hotKeys.add(new HotKeyTracker());
        } else if (!(hotKeyTracker = this.getLatestHotKeyTracker()).hasEnded() && currentItem == Core.mainHotBarSlot.getValue() - 1) {
            hotKeyTracker.end();
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
                    this.getOpponent(player).onOpponentHit(criticalHit);
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
        EntityPlayer closestPlayer = getClosestPlayer();
        if (thePlayer != null && closestPlayer != null && thePlayer.getDistanceToEntity(closestPlayer) <= 3.01) {
            this.attacksSent++;
            this.getOpponent(closestPlayer).attacksSent++;
        }

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

        StrafingTracker strafingTracker;
        if (state) {
            if (leftIsDown && !rightIsDown) {
                if (strafes.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded()) strafingTracker.end();
                this.strafes.add(new StrafingTracker(false));
            } else if (!leftIsDown && rightIsDown) {
                this.strafes.add(new StrafingTracker(true));
            } else if (rightIsDown && this.strafes.size() != 0 && !this.getLatestStrafingTracker().hasEnded()) {
                this.getLatestStrafingTracker().end();
            }
        } else {
            if (leftIsDown && !rightIsDown) {
                if (strafes.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded()) strafingTracker.end();
                this.strafes.add(new StrafingTracker(false));
            } else if (!leftIsDown && rightIsDown) {
                if (strafes.size() != 0 && !(strafingTracker = this.getLatestStrafingTracker()).hasEnded()) strafingTracker.end();
                this.strafes.add(new StrafingTracker(true));
            } else if (strafes.size() != 0) {
                if (!this.getLatestStrafingTracker().hasEnded()) {
                    this.getLatestStrafingTracker().end();
                }
            }
        }
    }

    private StrafingTracker getLatestStrafingTracker() {
        return strafes.get(strafes.size() - 1);
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
    }

    @Override
    public String toString() {
        JsonObject asJsonObject = new JsonObject();

        JsonObject information = new JsonObject();

        information.addProperty("id", id);
        information.addProperty("startTime", startTime);
        information.addProperty("endTime", endTime);
        information.addProperty("serverIp", serverIp);

        return asJsonObject.toString();
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

    public List<PotionEffectTracker> getPotionEffects() {
        return potionEffects;
    }

    public List<StrafingTracker> getStrafes() {
        return this.strafes;
    }

    public List<HotKeyTracker> getHotKeys() {
        return this.hotKeys;
    }

    public static class Opponent {
        private final EntityPlayer opponent;
        private final String name;
        private int attacksSent = 0;
        private int attacksLanded = 0;
        private int criticalAttacksLanded = 0;
        private int opponentAttacksTaken = 0;

        private final List<ComboTracker> combos = new ArrayList<>();

        private int opponentMeleeCombo = 0;
        private int meleeCombo = 0;

        private int hitsTaken = 0;
        private int projectilesTaken = 0;
        private int arrowsTaken = 0;

        private int arrowsHit = 0;
        private int projectilesHit = 0;

        private boolean opponentHitByFishingHook = false;

        public Opponent(EntityPlayer opponent) {
            this.opponent = opponent;
            this.name = opponent.getName();
            combos.add(new ComboTracker());
        }

        public void tick() {
            List<Entity> closestOpponentEntities = Minecraft.getMinecraft().theWorld.
                    getEntitiesWithinAABBExcludingEntity(
                            opponent,
                            opponent.getEntityBoundingBox().addCoord(opponent.motionX, opponent.motionY, opponent.motionZ).expand(1.0D, 1.0D, 1.0D));

            for (Entity entity : closestOpponentEntities) {
                this.opponentHitByFishingHook = entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != opponent.getUniqueID();
            }
        }

        private void onOpponentHit(boolean criticalHit) {
            this.opponentAttacksTaken++;
            this.meleeCombo++;
            this.opponentMeleeCombo = 0;
            if (criticalHit) this.criticalAttacksLanded++;
            this.getLatestComboTracker().incrementComboCount();
        }

        private void onDamage() {
            this.hitsTaken++;
            this.opponentMeleeCombo++;
            this.meleeCombo = 0;
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

        public void end() {
            this.endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return this.endTime != 0;
        }

        public int getIndex() {
            return this.index;
        }

        public Item getItem() {
            return this.itemStack == null ? null : this.itemStack.getItem();
        }

        public long getStartTime() {
            return this.startTime;
        }

        public long getEndTime() {
            return this.endTime;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
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
    }

    public static class StrafingTracker {
        private final long startTime;
        private long endTime = 0;
        private final boolean isRightStrafe;

        public StrafingTracker(boolean isRightStrafe) {
            this.isRightStrafe = isRightStrafe;
            this.startTime = System.currentTimeMillis();
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
    }

    public static class ComboTracker {
        private long startTime;
        private long endTime = 0;
        private int comboCount = 0;

        public ComboTracker() {
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
    }
}
