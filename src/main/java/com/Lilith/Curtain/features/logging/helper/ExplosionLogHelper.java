package com.Lilith.Curtain.features.logging.helper;

import static com.Lilith.Curtain.utils.Messenger.c;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Explosion;

import com.Lilith.Curtain.features.logging.AbstractLogger;
import com.Lilith.Curtain.features.logging.LoggerManager;
import com.Lilith.Curtain.utils.Messenger;

public class ExplosionLogHelper {

    private final boolean createFire;
    public final double posX;
    public final double posY;
    public final double posZ;
    private final float power;
    private boolean affectBlocks = false;
    private final Map<EntityChangedStatusWithCount, Integer> impactedEntities = new LinkedHashMap<EntityChangedStatusWithCount, Integer>();

    private static long lastGametime = 0;
    private static int explosionCountInCurrentGT = 0;
    private static boolean newTick;

    private static List<IChatComponent> log = new ArrayList<IChatComponent>();

    public ExplosionLogHelper(double x, double y, double z, float power, boolean createFire) {
        this.power = power;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.createFire = createFire;
    }

    public ExplosionLogHelper(Explosion explosion) {
        this(
            explosion.explosionX,
            explosion.explosionY,
            explosion.explosionZ,
            explosion.explosionSize,
            explosion.isFlaming);
    }

    public void setAffectBlocks(boolean b) {
        affectBlocks = b;
    }

    public void onExplosionDone(long gametime) {
        newTick = false;
        if (lastGametime != gametime) {
            explosionCountInCurrentGT = 0;
            lastGametime = gametime;
            newTick = true;
        }
        explosionCountInCurrentGT++;

        List<IChatComponent> messages = new ArrayList<IChatComponent>();
        if (newTick) messages.add(c("wb tick : ", "d " + gametime));

        messages.add(c("d #" + explosionCountInCurrentGT, "gb ->", Messenger.dblt("l", posX, posY, posZ)));
        messages.add(c("w   affects blocks: ", "m " + this.affectBlocks));
        messages.add(c("w   creates fire: ", "m " + this.createFire));
        messages.add(c("w   power: ", "c " + this.power));
        if (impactedEntities.isEmpty()) {
            messages.add(c("w   affected entities: ", "m None"));
        } else {
            messages.add(c("w   affected entities:"));
            for (Map.Entry<EntityChangedStatusWithCount, Integer> e : impactedEntities.entrySet()) {
                EntityChangedStatusWithCount k = e.getKey();
                int v = e.getValue();
                boolean samePos = k.posX == posX && k.posY == posY && k.posZ == posZ;
                messages.add(
                    c(
                        samePos ? "r   - TNT" : "w   - ",
                        Messenger.dblt(samePos ? "r" : "y", k.posX, k.posY, k.posZ),
                        "w  dV",
                        Messenger.dblt("d", k.accelX, k.accelY, k.accelZ),
                        "w  " + k.type,
                        (v > 1) ? "l (" + v + ")" : ""));
            }
        }

        // 1.7.10 cannot render line breaks inside one component, keep the lines separate
        log = messages;
        LoggerManager.ableSendToChat("explosion");
    }

    public void onEntityImpacted(Entity entity, double accelX, double accelY, double accelZ) {
        EntityChangedStatusWithCount ent = new EntityChangedStatusWithCount(entity, accelX, accelY, accelZ);
        Integer count = impactedEntities.get(ent);
        impactedEntities.put(ent, count == null ? 1 : count + 1);
    }

    public static class ExplosionLogger extends AbstractLogger {

        public ExplosionLogger() {
            super("explosion");
        }

        @Override
        public List<IChatComponent> display(EntityPlayerMP player) {
            return log;
        }
    }

    public static class EntityChangedStatusWithCount {

        public final double posX;
        public final double posY;
        public final double posZ;
        public final String type;
        public final double accelX;
        public final double accelY;
        public final double accelZ;

        public EntityChangedStatusWithCount(Entity e, double accelX, double accelY, double accelZ) {
            this(e.posX, e.posY, e.posZ, String.valueOf(EntityList.getEntityString(e)), accelX, accelY, accelZ);
        }

        public EntityChangedStatusWithCount(double posX, double posY, double posZ, String type, double accelX,
            double accelY, double accelZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.type = type;
            this.accelX = accelX;
            this.accelY = accelY;
            this.accelZ = accelZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EntityChangedStatusWithCount)) return false;
            EntityChangedStatusWithCount other = (EntityChangedStatusWithCount) o;
            return Double.compare(other.posX, posX) == 0 && Double.compare(other.posY, posY) == 0
                && Double.compare(other.posZ, posZ) == 0
                && Double.compare(other.accelX, accelX) == 0
                && Double.compare(other.accelY, accelY) == 0
                && Double.compare(other.accelZ, accelZ) == 0
                && (type == null ? other.type == null : type.equals(other.type));
        }

        @Override
        public int hashCode() {
            int result = 1;
            long bits = Double.doubleToLongBits(posX);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            bits = Double.doubleToLongBits(posY);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            bits = Double.doubleToLongBits(posZ);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            result = 31 * result + (type == null ? 0 : type.hashCode());
            return result;
        }
    }

    /** Unused placeholder, kept for symmetry with the 1.20 sources. */
    @SuppressWarnings("unused")
    private static boolean isTnt(Entity entity) {
        return entity instanceof EntityTNTPrimed;
    }
}
