package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;

import java.util.ArrayList;
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
        if (sessionIsRunning) getLatestAnalysis().arrowShot();
    }

    public static void onArrowHit(EntityPlayer target) {
        attackTimer = 5;
        attackType = 1;
        if (sessionIsRunning) getLatestAnalysis().arrowHit(target);
    }

    public static void onHitByArrow(EntityPlayer shooter) {
        if (sessionIsRunning) getLatestAnalysis().hitByArrow(shooter);
    }

    public static void onProjectileThrown(int type) {
        if (sessionIsRunning) getLatestAnalysis().projectileThrown(type);
    }

    public static void onProjectileHit(EntityPlayer target) {
        attackTimer = 5;
        attackType = 2;
        if (sessionIsRunning) getLatestAnalysis().projectileHit(target);
    }

    public static void onHitByProjectile(EntityPlayer thrower) {
        if (sessionIsRunning) getLatestAnalysis().hitByProjectile(thrower, 1);
    }

    public static void onAttack(EntityPlayer target) {
        attackTimer = 5;
        attackType = 0;
        if (sessionIsRunning) getLatestAnalysis().attack(target);
    }

    public static void onPlayerHurt(EntityPlayer player) {
        if ((attackTimer > 0 && Core.automaticSessions.getValue()) || sessionIsRunning) {
            if (!sessionIsRunning) {
                switch (attackType) {
                    case 0:
                        getLatestAnalysis().attack(player);
                        break;
                    case 1:
                        getLatestAnalysis().arrowHit(player);
                        break;
                    case 2:
                        getLatestAnalysis().projectileHit(player);
                        break;
                }
            }
            getLatestAnalysis().playerHurt(player);
        }
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

    private long startTime;
    private long endTime;
    private int ticksSinceAction = 0;
    private int lastAttackType;
    private int lastAttackTimer = 0;
    private EntityPlayer opponent;

    private int leftClicks = 0;
    private int rightClicks = 0;

    private int attacksSent = 0;
    private int attacksLanded = 0;
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

    public CombatSession() {

    }

    public void activate() {
        startTime = System.currentTimeMillis();
        sessionIsRunning = true;
        EntityPlayer closestPlayer = null;
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (Minecraft.getMinecraft().thePlayer.getUniqueID() != player.getUniqueID() && (closestPlayer == null ||
                    (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(player) < Minecraft.getMinecraft().thePlayer.getDistanceToEntity(closestPlayer)
                            && !player.isInvisible()))) {
                closestPlayer = player;
            }
        }
        opponent = closestPlayer;
    }

    public void tick() {
        ticksSinceAction++;

        if (lastAttackTimer > 0) {
            lastAttackTimer--;
        } else {
            attackType = Integer.MIN_VALUE;
        }

        if (opponent == null) {
            end();
            return;
        }

        EntityPlayerSP thePlayer;
        if ((thePlayer = Minecraft.getMinecraft().thePlayer) == null) return;
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

        if (!Minecraft.getMinecraft().theWorld.playerEntities.contains(opponent)) {
            end();
            return;
        } else if (thePlayer.getDistance(thePlayer.prevPosX, thePlayer.prevPosY, thePlayer.prevPosZ) > 50) {
            end();
            return;
        } else if (opponent.isDead) {
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
    }

    public void updateTicksSinceAction() {
        ticksSinceAction = 0;
    }

    public void arrowShot() {
        updateTicksSinceAction();
        arrowsShot++;
    }

    public void arrowHit(EntityPlayer target) {
        updateTicksSinceAction();
        lastAttackType = 1;
        lastAttackTimer = 5;
    }

    public void hitByArrow(EntityPlayer shooter) {
        updateTicksSinceAction();
        hitByArrow = false;
        arrowsTaken++;
    }

    public void projectileThrown(int type) {
        updateTicksSinceAction();
        projectilesThrown++;
    }

    public void projectileHit(EntityPlayer target) {
        updateTicksSinceAction();
        lastAttackType = 2;
        lastAttackTimer = 5;
    }

    public void hitByProjectile(EntityPlayer thrower, int source) {
        updateTicksSinceAction();
        hitByFishingHook = false;
        if (source == 1 && thrower.fishEntity != null) return;
        projectilesTaken++;
    }

    public void attack(EntityPlayer target) {
        updateTicksSinceAction();
        lastAttackType = 0;
        lastAttackTimer = 5;
        attacksLanded++;
    }

    public void playerHurt(EntityPlayer player) {
        updateTicksSinceAction();
        if (opponentHitByFishingHook) {
            projectilesHit++;
            return;
        }

        if (attackTimer > 0) {
            switch (lastAttackType) {
                case 0:
                    opponentAttacksTaken++;
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

    public void end() {
        endTime = System.currentTimeMillis();
        sessionIsRunning = false;
    }
}
