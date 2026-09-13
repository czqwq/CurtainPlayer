package com.Lilith.Curtain.features.player.helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;
import com.Lilith.Curtain.utils.Tracer;

public class EntityPlayerActionPack {

    private final EntityPlayerMP player;

    private final Map<ActionType, Action> actions = new TreeMap<ActionType, Action>();

    private boolean isHittingBlock;
    private int currentX = Integer.MIN_VALUE;
    private int currentY;
    private int currentZ;
    private int blockHitDelay;
    private float curBlockDamageMP;

    private boolean sneaking;
    private boolean sprinting;
    private float forward;
    private float strafing;

    private int itemUseCooldown;

    public EntityPlayerActionPack(EntityPlayerMP playerIn) {
        player = playerIn;
        stopAll();
    }

    public void copyFrom(EntityPlayerActionPack other) {
        actions.putAll(other.actions);
        currentX = other.currentX;
        currentY = other.currentY;
        currentZ = other.currentZ;
        blockHitDelay = other.blockHitDelay;
        isHittingBlock = other.isHittingBlock;
        curBlockDamageMP = other.curBlockDamageMP;
        sneaking = other.sneaking;
        sprinting = other.sprinting;
        forward = other.forward;
        strafing = other.strafing;
        itemUseCooldown = other.itemUseCooldown;
    }

    public EntityPlayerActionPack start(ActionType type, Action action) {
        Action previous = actions.remove(type);
        if (previous != null) type.stop(player, previous);
        if (action != null) {
            actions.put(type, action);
            type.start(player, action);
        }
        return this;
    }

    public EntityPlayerActionPack setSneaking(boolean doSneak) {
        sneaking = doSneak;
        player.setSneaking(doSneak);
        if (sprinting && sneaking) setSprinting(false);
        return this;
    }

    public EntityPlayerActionPack setSprinting(boolean doSprint) {
        sprinting = doSprint;
        player.setSprinting(doSprint);
        if (sneaking && sprinting) setSneaking(false);
        return this;
    }

    public EntityPlayerActionPack setForward(float value) {
        forward = value;
        return this;
    }

    public EntityPlayerActionPack setStrafing(float value) {
        strafing = value;
        return this;
    }

    public EntityPlayerActionPack look(ForgeDirection direction) {
        switch (direction) {
            case NORTH:
                return look(180, 0);
            case SOUTH:
                return look(0, 0);
            case EAST:
                return look(-90, 0);
            case WEST:
                return look(90, 0);
            case UP:
                return look(player.rotationYaw, -90);
            case DOWN:
                return look(player.rotationYaw, 90);
            default:
                return this;
        }
    }

    public EntityPlayerActionPack look(float yaw, float pitch) {
        player.rotationYaw = yaw % 360;
        player.rotationPitch = MathHelper.clamp_float(pitch, -90, 90);
        player.rotationYawHead = player.rotationYaw;
        player.prevRotationYaw = player.rotationYaw;
        return this;
    }

    public EntityPlayerActionPack lookAt(Vec3 position) {
        double dx = position.xCoord - player.posX;
        double dy = position.yCoord - (player.posY + player.getEyeHeight());
        double dz = position.zCoord - player.posZ;
        double dist = MathHelper.sqrt_double(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, dist) * 180.0D / Math.PI));
        return look(yaw, pitch);
    }

    public EntityPlayerActionPack turn(float yaw, float pitch) {
        return look(player.rotationYaw + yaw, player.rotationPitch + pitch);
    }

    public EntityPlayerActionPack stopMovement() {
        setSneaking(false);
        setSprinting(false);
        forward = 0.0F;
        strafing = 0.0F;
        return this;
    }

    public EntityPlayerActionPack stopAll() {
        for (ActionType type : actions.keySet()) type.stop(player, actions.get(type));
        actions.clear();
        return stopMovement();
    }

    @SuppressWarnings("unchecked")
    public EntityPlayerActionPack mount(boolean onlyRideables) {
        List<Entity> entities;
        if (onlyRideables) {
            entities = player.worldObj
                .getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(3.0D, 1.0D, 3.0D));
        } else {
            entities = player.worldObj
                .getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(3.0D, 1.0D, 3.0D));
        }
        if (entities.isEmpty()) return this;
        Entity closest = null;
        double distance = Double.POSITIVE_INFINITY;
        Entity currentVehicle = player.ridingEntity;
        for (Entity e : entities) {
            if (e == player || currentVehicle == e) continue;
            if (onlyRideables && !(e instanceof EntityMinecart || e instanceof EntityBoat || e instanceof EntityHorse))
                continue;
            double dd = player.getDistanceSqToEntity(e);
            if (dd < distance) {
                distance = dd;
                closest = e;
            }
        }
        if (closest == null) return this;
        if (closest instanceof EntityHorse) {
            closest.interactFirst(player);
        } else {
            player.mountEntity(closest);
        }
        return this;
    }

    public EntityPlayerActionPack dismount() {
        player.mountEntity(null);
        return this;
    }

    public void onUpdate() {
        Map<ActionType, Boolean> actionAttempts = new HashMap<ActionType, Boolean>();
        Iterator<Map.Entry<ActionType, Action>> iterator = actions.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            if (iterator.next()
                .getValue().done) iterator.remove();
        }
        for (Map.Entry<ActionType, Action> e : actions.entrySet()) {
            Action action = e.getValue();
            if (!(Boolean.TRUE.equals(actionAttempts.get(ActionType.USE)) && e.getKey() == ActionType.ATTACK)) {
                Boolean actionStatus = action.tick(this, e.getKey());
                if (actionStatus != null) actionAttempts.put(e.getKey(), actionStatus);
            }
            if (e.getKey() == ActionType.ATTACK && Boolean.TRUE.equals(actionAttempts.get(ActionType.ATTACK))
                && !Boolean.TRUE.equals(actionAttempts.get(ActionType.USE))) {
                Action using = actions.get(ActionType.USE);
                if (using != null) using.retry(this, ActionType.USE);
            }
        }
        float vel = sneaking ? 0.3F : 1.0F;
        if (forward != 0.0F || player instanceof EntityPlayerMPFake) {
            player.moveForward = forward * vel;
        }
        if (strafing != 0.0F || player instanceof EntityPlayerMPFake) {
            player.moveStrafing = strafing * vel;
        }
        if (blockHitDelay > 0) blockHitDelay--;
    }

    static MovingObjectPosition getTarget(EntityPlayerMP player) {
        double reach = player.theItemInWorldManager.isCreative() ? 5 : 4.5f;
        return Tracer.rayTrace(player, 1, reach, false);
    }

    private void dropItemFromSlot(int slot, boolean dropAll) {
        InventoryPlayer inv = player.inventory;
        if (slot < 0 || slot >= inv.mainInventory.length) return;
        ItemStack stack = inv.mainInventory[slot];
        if (stack == null) return;
        int amount = dropAll ? stack.stackSize : 1;
        ItemStack dropped = inv.decrStackSize(slot, amount);
        if (dropped != null) {
            player.dropPlayerItemWithRandomChoice(dropped, false);
        }
    }

    public void drop(int selectedSlot, boolean dropAll) {
        InventoryPlayer inv = player.inventory;
        if (selectedSlot == -2) {
            for (int i = inv.mainInventory.length - 1; i >= 0; i--) dropItemFromSlot(i, dropAll);
        } else {
            if (selectedSlot == -1) selectedSlot = inv.currentItem;
            dropItemFromSlot(selectedSlot, dropAll);
        }
    }

    public void setSlot(int slot) {
        player.inventory.currentItem = slot - 1;
    }

    public enum ActionType {

        USE(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((IServerPlayer) player).getActionPack();
                if (ap.itemUseCooldown > 0) {
                    ap.itemUseCooldown--;
                    return true;
                }
                if (player.isUsingItem()) {
                    return true;
                }
                MovingObjectPosition hit = getTarget(player);
                ItemStack stack = player.getCurrentEquippedItem();
                if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    int x = hit.blockX;
                    int y = hit.blockY;
                    int z = hit.blockZ;
                    int side = hit.sideHit;
                    float hx = (float) (hit.hitVec.xCoord - x);
                    float hy = (float) (hit.hitVec.yCoord - y);
                    float hz = (float) (hit.hitVec.zCoord - z);
                    if (y < 255 && player.worldObj.canMineBlock(player, x, y, z)) {
                        if (player.theItemInWorldManager
                            .activateBlockOrUseItem(player, player.worldObj, stack, x, y, z, side, hx, hy, hz)) {
                            player.swingItem();
                            ap.itemUseCooldown = 3;
                            return true;
                        }
                    }
                } else if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    Entity entity = hit.entityHit;
                    if (player.interactWith(entity)) {
                        ap.itemUseCooldown = 3;
                        return true;
                    }
                    if (stack != null && entity instanceof net.minecraft.entity.EntityLivingBase
                        && stack.interactWithEntity(player, (net.minecraft.entity.EntityLivingBase) entity)) {
                        ap.itemUseCooldown = 3;
                        return true;
                    }
                }
                if (player.theItemInWorldManager.tryUseItem(player, player.worldObj, stack)) {
                    ap.itemUseCooldown = 3;
                    return true;
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((IServerPlayer) player).getActionPack();
                ap.itemUseCooldown = 0;
            }
        },
        ATTACK(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                MovingObjectPosition hit = getTarget(player);
                if (hit == null) return false;
                if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    if (!action.isContinuous) {
                        player.attackTargetEntityWithCurrentItem(hit.entityHit);
                        player.swingItem();
                    }
                    return true;
                }
                if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    EntityPlayerActionPack ap = ((IServerPlayer) player).getActionPack();
                    if (ap.blockHitDelay > 0) {
                        ap.blockHitDelay--;
                        return false;
                    }
                    int x = hit.blockX;
                    int y = hit.blockY;
                    int z = hit.blockZ;
                    int side = hit.sideHit;
                    if (ap.currentX == x && ap.currentY == y
                        && ap.currentZ == z
                        && player.worldObj.isAirBlock(x, y, z)) {
                        ap.currentX = Integer.MIN_VALUE;
                        return false;
                    }
                    if (player.theItemInWorldManager.isCreative()) {
                        player.theItemInWorldManager.onBlockClicked(x, y, z, side);
                        ap.blockHitDelay = 5;
                        player.swingItem();
                        return true;
                    }
                    if (ap.currentX != x || ap.currentY != y || ap.currentZ != z) {
                        player.theItemInWorldManager.onBlockClicked(x, y, z, side);
                        ap.currentX = x;
                        ap.currentY = y;
                        ap.currentZ = z;
                    }
                    player.swingItem();
                    return false;
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((IServerPlayer) player).getActionPack();
                if (ap.currentX == Integer.MIN_VALUE) return;
                player.theItemInWorldManager.cancelDestroyingBlock(ap.currentX, ap.currentY, ap.currentZ);
                ap.currentX = Integer.MIN_VALUE;
            }
        },
        JUMP(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                if (action.limit == 1) {
                    if (player.onGround) player.jump();
                } else {
                    player.setJumping(true);
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                player.setJumping(false);
            }
        },
        DROP_ITEM(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                player.dropOneItem(false);
                return false;
            }
        },
        DROP_STACK(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                player.dropOneItem(true);
                return false;
            }
        },
        SWAP_HANDS(true) {

            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                // 1.7.10 has no offhand, there is nothing to swap with
                return false;
            }
        };

        public final boolean preventSpectator;

        ActionType(boolean preventSpectator) {
            this.preventSpectator = preventSpectator;
        }

        void start(EntityPlayerMP player, Action action) {}

        abstract boolean execute(EntityPlayerMP player, Action action);

        void inactiveTick(EntityPlayerMP player, Action action) {}

        void stop(EntityPlayerMP player, Action action) {
            inactiveTick(player, action);
        }
    }

    public static class Action {

        public boolean done = false;
        public final int limit;
        public final int interval;
        public final int offset;
        private int count;
        private int next;
        private final boolean isContinuous;

        private Action(int limit, int interval, int offset, boolean continuous) {
            this.limit = limit;
            this.interval = interval;
            this.offset = offset;
            this.next = interval + offset;
            this.isContinuous = continuous;
        }

        public static Action once() {
            return new Action(1, 1, 0, false);
        }

        public static Action continuous() {
            return new Action(-1, 1, 0, true);
        }

        public static Action interval(int interval) {
            return new Action(-1, interval, 0, false);
        }

        public static Action interval(int interval, int offset) {
            return new Action(-1, interval, offset, false);
        }

        Boolean tick(EntityPlayerActionPack actionPack, ActionType type) {
            this.next--;
            Boolean cancel = null;
            if (this.next <= 0) {
                if (this.interval == 1 && !this.isContinuous) {
                    type.inactiveTick(actionPack.player, this);
                }
                cancel = type.execute(actionPack.player, this);
                this.count++;
                if (this.count == this.limit) {
                    type.stop(actionPack.player, null);
                    this.done = true;
                    return cancel;
                }
                this.next = this.interval;
            } else {
                type.inactiveTick(actionPack.player, this);
            }
            return cancel;
        }

        void retry(EntityPlayerActionPack actionPack, ActionType type) {
            type.execute(actionPack.player, this);
            this.count++;
            if (this.count == this.limit) {
                type.stop(actionPack.player, null);
                this.done = true;
            }
        }
    }

    /** Unused, kept for API compatibility with the 1.20 sources. */
    @SuppressWarnings("unused")
    private static boolean isFake(EntityPlayer player) {
        return player instanceof EntityPlayerMPFake;
    }

    @SuppressWarnings("unused")
    private static float unusedClamp(float v) {
        return MathHelper.clamp_float(v, -90, 90);
    }
}
