package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.utilities.Chat;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CombatSession {
    private static final List<CombatSession> combatSessions = new ArrayList<>();
    private static boolean sessionIsRunning = false;
    private static int attackTimer = 0;
    private static int attackType;

    public static List<CombatSession> getCombatSessions() {
        return combatSessions;
    }

    public static boolean sessionIsRunning() {
        return sessionIsRunning;
    }

    public static void createNewCombatSession() {
        combatSessions.add(new CombatSession());
    }

    public static CombatSession createAndActivate() {
        CombatSession combatSession = new CombatSession();
        combatSession.activate();
        combatSessions.add(combatSession);
        return combatSession;
    }

    public static CombatSession getLatestAnalysis() {
        return combatSessions.size() == 0 || !sessionIsRunning ? createAndActivate() : combatSessions.get(combatSessions.size() - 1);
    }

    public static void onWorldLoad() {
        if (sessionIsRunning) combatSessions.get(combatSessions.size() - 1).end();
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
        if (Core.automaticSessions.getValue() || sessionIsRunning) {
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
        if (sessionIsRunning) getLatestAnalysis().projectileThrown(type);
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

    public static int getLeftClicks() {
        return sessionIsRunning ? getLatestAnalysis().leftClicks : 0;
    }

    public static int getRightClicks() {
        return sessionIsRunning ? getLatestAnalysis().rightClicks : 0;
    }

    public static int getAttacksSent() {
        return sessionIsRunning ? getLatestAnalysis().attacksSent : 0;
    }

    public static int getAttacksLanded() {
        return sessionIsRunning ? getLatestAnalysis().attacksLanded : 0;
    }

    public static int getOpponentHitsTaken() {
        return sessionIsRunning ? getLatestAnalysis().opponentAttacksTaken : 0;
    }

    public static String getAttackAccuracy() {
        return !sessionIsRunning || getAttacksSent() == 0 ? "N/A" : Text.shortenDouble(getAttacksLanded() / (double) getAttacksSent() * 100, 1) + "%";
    }

    public static int getProjectilesThrown() {
        return sessionIsRunning ? getLatestAnalysis().projectilesThrown : 0;
    }

    public static int getProjectilesHit() {
        return sessionIsRunning ? getLatestAnalysis().projectilesHit : 0;
    }

    public static String getProjectileAccuracy() {
        return !sessionIsRunning || getProjectilesThrown() == 0 ? "N/A" : Text.shortenDouble(getProjectilesHit() / (double) getProjectilesThrown() * 100, 1) + "%";
    }

    public static int getArrowsShot() {
        return sessionIsRunning ? getLatestAnalysis().arrowsShot : 0;
    }

    public static int getArrowsHit() {
        return sessionIsRunning ? getLatestAnalysis().arrowsHit : 0;
    }

    public static String getArrowAccuracy() {
        return !sessionIsRunning || getArrowsShot() == 0 ? "N/A" : Text.shortenDouble(getArrowsHit() / (double) getArrowsShot() * 100, 1) + "%";
    }

    public static int getHitsTaken() {
        return sessionIsRunning ? getLatestAnalysis().hitsTaken : 0;
    }

    public static int getProjectilesTaken() {
        return sessionIsRunning ? getLatestAnalysis().projectilesTaken : 0;
    }

    public static int getArrowsTaken() {
        return sessionIsRunning ? getLatestAnalysis().arrowsTaken : 0;
    }

    private int id = combatSessions.size();
    private long startTime;
    private long endTime;
    private final String serverIp = Minecraft.getMinecraft().getCurrentServerData().serverIP;

    private int ticksSinceAction = 0;
    private int lastAttackType;
    private int lastAttackTimer = 0;
    private EntityPlayer opponent;

    private int leftClicks = 0;
    private int rightClicks = 0;

    private int attacksSent = 0;
    private int attacksLanded = 0;
    private boolean criticalHit = false;
    private int criticalAttacksLanded = 0;
    private int opponentAttacksTaken = 0;

    private int projectilesThrown = 0;
    private int projectilesHit = 0;

    private int arrowsShot = 0;
    private int arrowsHit = 0;

    private int hitsTaken = 0;
    private int arrowsTaken = 0;
    private boolean hitByArrow = false;
    private int projectilesTaken = 0;
    private boolean hitByFishingHook = false;
    private boolean opponentHitByFishingHook = false;

    private ItemStack[] startingInventory;
    private ItemStack[] startingArmor;

    private ItemStack[] endingInventory;
    private ItemStack[] endingArmor;

    // More fields such as armor and inventory items and potion effects
    private Collection<PotionEffect> previousPotionEffects;
    private final List<PotionEffectTracker> potionEffects = new ArrayList<>();

    // Strafing data

    // Proper w-tapping/blockhitting/projectile usage

    // Hotkey time
    private int lastHeldItemIndex;
    private final List<HotKeyTracker> hotKeys = new ArrayList<>();

    public CombatSession() {

    }

    public void activate() {
        startTime = System.currentTimeMillis();
        sessionIsRunning = true;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        EntityPlayer closestPlayer = null;
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (Minecraft.getMinecraft().thePlayer.getUniqueID() != player.getUniqueID() && (closestPlayer == null ||
                    (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(player) < Minecraft.getMinecraft().thePlayer.getDistanceToEntity(closestPlayer)
                            && !player.isInvisible()))) {
                closestPlayer = player;
            }
        }
        opponent = closestPlayer;

        startingInventory = thePlayer.inventory.mainInventory.clone();
        startingArmor = thePlayer.inventory.armorInventory.clone();
        lastHeldItemIndex = thePlayer.inventory.currentItem;

        previousPotionEffects = thePlayer.getActivePotionEffects();
    }

    public void tick() {
        ticksSinceAction++;

        if (lastAttackTimer > 0) {
            lastAttackTimer--;
        } else {
            attackType = Integer.MIN_VALUE;
        }

        if (opponent == null || Minecraft.getMinecraft().thePlayer == null) {
            end();
            return;
        }

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        List<Entity> closestEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        thePlayer,
                        thePlayer.getEntityBoundingBox().addCoord(thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ).expand(1.0D, 1.0D, 1.0D));

        for (Entity entity : closestEntities) {
            if (!hitByArrow) {
                if (entity instanceof EntityArrow && ((EntityArrow) entity).shootingEntity.getUniqueID() != thePlayer.getUniqueID()) {
                    hitByArrow = true;
                }
            }
            hitByFishingHook = entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != thePlayer.getUniqueID();
        }

        if (!Minecraft.getMinecraft().theWorld.playerEntities.contains(opponent) || opponent.isDead) {
            end();
            return;
        } else if (thePlayer.getDistance(thePlayer.prevPosX, thePlayer.prevPosY, thePlayer.prevPosZ) > 50) {
            end();
            return;
        } else if (thePlayer.capabilities.allowFlying || thePlayer.isInvisible()) {
            end();
            return;
        }

        List<Entity> closestOpponentEntities = Minecraft.getMinecraft().theWorld.
                getEntitiesWithinAABBExcludingEntity(
                        opponent,
                        opponent.getEntityBoundingBox().addCoord(opponent.motionX, opponent.motionY, opponent.motionZ).expand(1.0D, 1.0D, 1.0D));

        for (Entity entity : closestOpponentEntities) {
            opponentHitByFishingHook = entity instanceof EntityFishHook && ((EntityFishHook) entity).angler.getUniqueID() != opponent.getUniqueID();
        }

        if (lastHeldItemIndex != thePlayer.inventory.currentItem) {
            heldItemChange();
        }
        lastHeldItemIndex = thePlayer.inventory.currentItem;

        if (!previousPotionEffects.equals(thePlayer.getActivePotionEffects())) {
            potionEffectChange();
        }
    }

    private void heldItemChange() {
        if (hotKeys.size() == 0) {
            if (Minecraft.getMinecraft().thePlayer.inventory.currentItem != Core.mainHotBarSlot.getValue() - 1)  {
                hotKeys.add(new HotKeyTracker(Minecraft.getMinecraft().thePlayer.getHeldItem()));
            }
        } else if (hotKeys.get(hotKeys.size() - 1).hasEnded() && Minecraft.getMinecraft().thePlayer.inventory.currentItem != Core.mainHotBarSlot.getValue() - 1) {
            hotKeys.add(new HotKeyTracker(Minecraft.getMinecraft().thePlayer.getHeldItem()));
        } else if (!hotKeys.get(hotKeys.size() - 1).hasEnded()) {
            hotKeys.get(hotKeys.size() - 1).end();
        }
    }

    private void potionEffectChange() {
        Collection<PotionEffect> newPotionEffects = Minecraft.getMinecraft().thePlayer.getActivePotionEffects();
        for (PotionEffectTracker potionEffectTracker : potionEffects) {
            if (!potionEffectTracker.hasEnded() && !newPotionEffects.contains(potionEffectTracker.getPotionEffect())) {
                potionEffectTracker.end();
            }
        }

        for (PotionEffect potionEffect : newPotionEffects) {
            if (!hasPotionEffectTracker(potionEffect)) {
                potionEffects.add(new PotionEffectTracker(potionEffect));
            }
        }
    }

    private boolean hasPotionEffectTracker(PotionEffect potionEffect) {
        for (PotionEffectTracker potionEffectTracker : potionEffects) {
            if (!potionEffectTracker.hasEnded() && potionEffectTracker.getPotionEffect().equals(potionEffect)) {
                return true;
            }
        }
        return false;
    }

    public void updateTicksSinceAction() {
        ticksSinceAction = 0;
    }

    public void arrowShot() {
        updateTicksSinceAction();
        arrowsShot++;
    }

    public void arrowHit(EntityPlayer target) {
        if (target.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        lastAttackType = 1;
        lastAttackTimer = 5;
    }

    public void hitByArrow(EntityPlayer shooter) {
        if (shooter.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        hitByArrow = false;
        arrowsTaken++;
    }

    public void projectileThrown(int type) {
        updateTicksSinceAction();
        projectilesThrown++;
    }

    public void projectileHit(EntityPlayer target) {
        if (target.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        lastAttackType = 2;
        lastAttackTimer = 5;
    }

    public void hitByProjectile(EntityPlayer thrower, int source) {
        if (thrower == null || thrower.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        hitByFishingHook = false;
        if (source == 1 && thrower.fishEntity != null) return;
        projectilesTaken++;
    }

    public void attack(EntityPlayer target) {
        if (target.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        lastAttackType = 0;
        lastAttackTimer = 5;
        attacksLanded++;
        criticalHit = Minecraft.getMinecraft().thePlayer.motionY < 0;
    }

    public void playerHurt(EntityPlayer player) {
        if (player.getUniqueID() != opponent.getUniqueID()) return;
        updateTicksSinceAction();
        if (opponentHitByFishingHook) {
            projectilesHit++;
            return;
        }

        if (lastAttackTimer > 0) {
            switch (lastAttackType) {
                case 0:
                    opponentAttacksTaken++;
                    if (criticalHit) criticalAttacksLanded++;
                    break;
                case 1:
                    arrowsHit++;
                    break;
                case 2:
                    projectilesHit++;
                    break;
            }
        }
    }

    public void hurt(EntityPlayer attacker) {
        updateTicksSinceAction();
        if (hitByArrow) {
            hitByArrow(attacker);
        } else if (hitByFishingHook) {
          hitByProjectile(attacker, 0);  
        } else {
            hitsTaken++;
        }
    }

    public void leftClick() {
        updateTicksSinceAction();
        leftClicks++;
        if (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(opponent) <= 3.01) attacksSent++;
    }

    public void rightClick() {
        updateTicksSinceAction();
        rightClicks++;
    }

    public void keyTyped(int keyCode, boolean state) {

    }

    public void end() {
        endTime = System.currentTimeMillis();
        sessionIsRunning = false;

        if (hotKeys.size() != 0 && !hotKeys.get(hotKeys.size() - 1).hasEnded()) {
            hotKeys.get(hotKeys.size() - 1).end();
        }

        EntityPlayerSP thePlayer;
        if ((thePlayer = Minecraft.getMinecraft().thePlayer) == null) return;
        endingInventory = thePlayer.inventory.mainInventory.clone();
        endingArmor = thePlayer.inventory.armorInventory.clone();
    }


    private static class HotKeyTracker {
        long startTime;
        long endTime = 0;
        ItemStack itemStack;

        public HotKeyTracker(ItemStack itemStack) {
            this.itemStack = itemStack;
            startTime = System.currentTimeMillis();
        }

        public void end() {
            endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return endTime != 0;
        }

        public Item getItem() {
            return itemStack == null ? null : itemStack.getItem();
        }
    }

    private static class PotionEffectTracker {
        long startTime;
        long endTime = 0;
        PotionEffect potionEffect;

        public PotionEffectTracker(PotionEffect potionEffect) {
            this.potionEffect = potionEffect;
            startTime = System.currentTimeMillis();
        }

        public void end() {
            endTime = System.currentTimeMillis();
        }

        public boolean hasEnded() {
            return endTime != 0;
        }

        public PotionEffect getPotionEffect() {
            return potionEffect;
        }
    }
}
