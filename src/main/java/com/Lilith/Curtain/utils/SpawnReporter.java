package com.Lilith.Curtain.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

import com.Lilith.Curtain.Curtain;

/**
 * 1.7.10 flavoured spawn reporter. The 1.20 version reads the vanilla spawn state; in 1.7.10 the
 * equivalent data is produced by {@code SpawnerAnimals} and {@code World#countEntities}.
 */
public class SpawnReporter {

    public static boolean mock_spawns = false;

    public static Long track_spawns = 0L;
    /** eligible spawning chunks per dimension */
    public static final HashMap<Integer, Integer> chunkCounts = new HashMap<Integer, Integer>();
    /** live mob counts per dimension and category, refreshed every spawning cycle */
    public static final HashMap<String, Integer> liveCounts = new HashMap<String, Integer>();

    public static final HashMap<String, HashMap<String, Long>> spawn_stats = new HashMap<String, HashMap<String, Long>>();
    public static double mobcap_exponent = 0.0D;

    public static final HashMap<String, Long> spawn_attempts = new HashMap<String, Long>();
    public static final HashMap<String, Long> overall_spawn_ticks = new HashMap<String, Long>();
    public static final HashMap<String, Long> spawn_ticks_full = new HashMap<String, Long>();
    public static final HashMap<String, Long> spawn_ticks_fail = new HashMap<String, Long>();
    public static final HashMap<String, Long> spawn_ticks_succ = new HashMap<String, Long>();
    public static final HashMap<String, Long> spawn_ticks_spawns = new HashMap<String, Long>();
    public static final HashMap<String, Long> spawn_cap_count = new HashMap<String, Long>();
    public static final HashMap<String, EvictingQueue<String>> spawned_mobs = new HashMap<String, EvictingQueue<String>>();
    public static final HashMap<EnumCreatureType, Integer> spawn_tries = new HashMap<EnumCreatureType, Integer>();
    public static int lowerSpawningX = Integer.MIN_VALUE, lowerSpawningY, lowerSpawningZ;
    public static int upperSpawningX = Integer.MAX_VALUE, upperSpawningY, upperSpawningZ;
    public static boolean hasSpawningBounds = false;

    public static HashMap<EnumCreatureType, Long> local_spawns = null;
    public static HashSet<EnumCreatureType> first_chunk_marker = null;

    static {
        reset_spawn_stats(null, true);
    }

    public static String key(int dimension, EnumCreatureType cat) {
        return dimension + ":" + cat.name();
    }

    private static long get(HashMap<String, Long> map, String key) {
        Long value = map.get(key);
        return value == null ? 0L : value;
    }

    private static void add(HashMap<String, Long> map, String key, long amount) {
        map.put(key, get(map, key) + amount);
    }

    public static void registerSpawn(EntityLiving mob, EnumCreatureType cat, int x, int y, int z) {
        if (hasSpawningBounds) {
            if (!(lowerSpawningX <= x && x <= upperSpawningX
                && lowerSpawningY <= y
                && y <= upperSpawningY
                && lowerSpawningZ <= z
                && z <= upperSpawningZ)) {
                return;
            }
        }
        int dim = mob.dimension;
        String key = key(dim, cat);
        HashMap<String, Long> stats = spawn_stats.get(key);
        if (stats == null) {
            stats = new HashMap<String, Long>();
            spawn_stats.put(key, stats);
        }
        String type = String.valueOf(net.minecraft.entity.EntityList.getEntityString(mob));
        Long count = stats.get(type);
        stats.put(type, count == null ? 1L : count + 1);
        EvictingQueue<String> queue = spawned_mobs.get(key);
        if (queue == null) {
            queue = new EvictingQueue<String>();
            spawned_mobs.put(key, queue);
        }
        queue.put(type + "@" + x + "," + y + "," + z);
        if (local_spawns == null) local_spawns = new HashMap<EnumCreatureType, Long>();
        Long local = local_spawns.get(cat);
        local_spawns.put(cat, local == null ? 1L : local + 1);
    }

    public static final int MAGIC_NUMBER = 256;

    /** The cap that vanilla 1.7.10 compares against, see SpawnerAnimals#findChunksForSpawning */
    public static int getSpawnCap(WorldServer world, EnumCreatureType cat) {
        int chunks = getChunkCount(world);
        return cat.getMaxNumberOfCreature() * chunks / MAGIC_NUMBER;
    }

    public static int getChunkCount(WorldServer world) {
        Integer count = chunkCounts.get(world.provider.dimensionId);
        if (count != null) return count;
        return world.getChunkProvider()
            .getLoadedChunkCount();
    }

    public static List<IChatComponent> printMobcapsForDimension(WorldServer world, boolean multiline) {
        int dimension = world.provider.dimensionId;
        String name = world.provider.getDimensionName();
        List<IChatComponent> lst = new ArrayList<IChatComponent>();
        if (multiline) lst.add(Messenger.s(String.format("Mobcaps for %s:", name)));
        int chunkcount = getChunkCount(world);
        if (chunkcount <= 0) {
            lst.add(Messenger.c("g   --UNAVAILABLE--"));
            return lst;
        }

        List<Object> shortCodes = new ArrayList<Object>();
        for (EnumCreatureType type : EnumCreatureType.values()) {
            int cur = world.countEntities(type, true);
            int max = getSpawnCap(world, type);
            String color = Messenger.heatmap_color(cur, max);
            String mobColor = Messenger.creatureTypeColor(type);
            if (multiline) {
                int rounds = spawn_tries.get(type) == null ? 1 : spawn_tries.get(type);
                lst.add(
                    Messenger.c(
                        String.format("w   %s: ", type.name()),
                        (cur < 0) ? "g -" : (color + " " + cur),
                        "g  / ",
                        mobColor + " " + max,
                        (rounds == 1) ? "w " : String.format("gi  (%d rounds/tick)", rounds)));
            } else {
                shortCodes.add(color + " " + cur);
                shortCodes.add("g /");
                shortCodes.add(mobColor + " " + max);
                shortCodes.add("g ,");
            }
        }
        if (!multiline) {
            if (!shortCodes.isEmpty()) {
                shortCodes.remove(shortCodes.size() - 1);
                lst.add(Messenger.c(shortCodes.toArray(new Object[0])));
            } else {
                lst.add(Messenger.c("g   --UNAVAILABLE--"));
            }
        }
        return lst;
    }

    public static List<IChatComponent> recent_spawns(World world, EnumCreatureType creature_type) {
        List<IChatComponent> lst = new ArrayList<IChatComponent>();
        if (track_spawns == 0L) {
            lst.add(Messenger.s("Spawn tracking not started"));
            return lst;
        }
        String type_code = creature_type.name();
        lst.add(Messenger.s(String.format("Recent %s spawns:", type_code)));
        EvictingQueue<String> queue = spawned_mobs.get(key(world.provider.dimensionId, creature_type));
        if (queue != null) {
            for (String entry : queue.keySet()) {
                lst.add(Messenger.c("w  - ", "wb " + entry));
            }
        }
        if (lst.size() == 1) {
            lst.add(Messenger.s(" - Nothing spawned yet, sorry."));
        }
        return lst;
    }

    public static List<IChatComponent> printEntitiesByType(EnumCreatureType cat, World worldIn, boolean all) {
        List<IChatComponent> lst = new ArrayList<IChatComponent>();
        lst.add(Messenger.s(String.format("Loaded entities for %s class:", cat.name())));
        for (Object o : worldIn.loadedEntityList) {
            Entity entity = (Entity) o;
            if (!(entity instanceof EntityLiving)) continue;
            if (getCreatureType(entity) != cat) continue;
            EntityLiving living = (EntityLiving) entity;
            boolean persistent = living.isNoDespawnRequired();
            if (!all && persistent) continue;
            lst.add(
                Messenger.c(
                    "w  - ",
                    Messenger.tp(
                        persistent ? "gb" : "wb",
                        MathHelper.floor_double(entity.posX),
                        MathHelper.floor_double(entity.posY),
                        MathHelper.floor_double(entity.posZ)),
                    String.format(
                        persistent ? "g : %s" : "w : %s",
                        net.minecraft.entity.EntityList.getEntityString(entity))));
        }
        if (lst.size() == 1) {
            lst.add(Messenger.s(" - Empty."));
        }
        return lst;
    }

    /** 1.7.10 does not expose the creature type, derive it the same way vanilla does. */
    public static EnumCreatureType getCreatureType(Entity entity) {
        if (entity instanceof EntityWaterMob) return EnumCreatureType.waterCreature;
        if (entity instanceof EntityAmbientCreature) return EnumCreatureType.ambient;
        if (entity instanceof IMob) return EnumCreatureType.monster;
        if (entity instanceof EntityAnimal) return EnumCreatureType.creature;
        return null;
    }

    public static void initialize_mocking() {
        mock_spawns = true;
    }

    public static void stop_mocking() {
        mock_spawns = false;
    }

    public static void reset_spawn_stats(MinecraftServer server, boolean full) {
        spawn_stats.clear();
        spawned_mobs.clear();
        liveCounts.clear();
        for (EnumCreatureType type : EnumCreatureType.values()) {
            if (full) spawn_tries.put(type, 1);
        }
        spawn_attempts.clear();
        overall_spawn_ticks.clear();
        spawn_ticks_full.clear();
        spawn_ticks_fail.clear();
        spawn_ticks_succ.clear();
        spawn_ticks_spawns.clear();
        spawn_cap_count.clear();
        track_spawns = 0L;
    }

    public static List<IChatComponent> tracking_report(World worldIn) {
        List<IChatComponent> report = new ArrayList<IChatComponent>();
        if (track_spawns == 0L) {
            report.add(Messenger.c("w Spawn tracking disabled, type '", "wi /spawn tracking start", "w ' to enable"));
            return report;
        }
        MinecraftServer server = Curtain.minecraftServer;
        long duration = (server == null ? 0L : server.getTickCounter()) - track_spawns;
        report.add(Messenger.c("bw --------------------"));
        String simulated = mock_spawns ? "[SIMULATED] " : "";
        report
            .add(Messenger.s(String.format("%sSpawn statistics: for %.1f min", simulated, (duration / 72000.0) * 60)));
        for (EnumCreatureType type : EnumCreatureType.values()) {
            for (int dim : new int[] { -1, 0, 1 }) {
                String code = key(dim, type);
                if (get(spawn_ticks_spawns, code) > 0L) {
                    double hours = get(overall_spawn_ticks, code) / 72000.0;
                    report.add(
                        Messenger.s(
                            String.format(
                                " > %s%s (%.1f min), %.1f m/t, %%{%.1fF %.1f- %.1f+}; %.2f s/att",
                                type.name()
                                    .substring(0, 3),
                                getWorldCode(dim),
                                60 * hours,
                                (1.0D * get(spawn_cap_count, code)) / Math.max(1L, get(spawn_attempts, code)),
                                (100.0D * get(spawn_ticks_full, code)) / Math.max(1L, get(spawn_attempts, code)),
                                (100.0D * get(spawn_ticks_fail, code)) / Math.max(1L, get(spawn_attempts, code)),
                                (100.0D * get(spawn_ticks_succ, code)) / Math.max(1L, get(spawn_attempts, code)),
                                (1.0D * get(spawn_ticks_spawns, code))
                                    / Math.max(1L, get(spawn_ticks_fail, code) + get(spawn_ticks_succ, code)))));
                    HashMap<String, Long> stats = spawn_stats.get(code);
                    if (stats != null) {
                        for (String mobName : stats.keySet()) {
                            report.add(
                                Messenger.s(
                                    String.format(
                                        "   - %s: %d spawns, %d per hour",
                                        mobName,
                                        stats.get(mobName),
                                        (72000 * stats.get(mobName) / Math.max(1L, duration)))));
                        }
                    }
                }
            }
        }
        return report;
    }

    private static String getWorldCode(int dim) {
        if (dim == 0) return "";
        String name = dim == -1 ? "NETHER" : (dim == 1 ? "END" : String.valueOf(dim));
        return "(" + name.charAt(0) + ")";
    }

    public static void killEntity(EntityLiving entity) {
        if (entity.ridingEntity != null) {
            entity.ridingEntity.setDead();
        }
        if (entity.riddenByEntity != null) {
            entity.riddenByEntity.setDead();
        }
        entity.setDead();
    }

    /**
     * Reports the spawn potential of the block position the command sender is looking at.
     */
    public static List<IChatComponent> report(int x, int y, int z, WorldServer worldIn) {
        List<IChatComponent> rep = new ArrayList<IChatComponent>();
        int lc = worldIn.getTopSolidOrLiquidBlock(x, z) + 1;
        String where = String.format((y >= lc) ? "%d blocks above it." : "%d blocks below it.", Math.abs(y - lc));
        if (y == lc) where = "right at it.";
        rep.add(Messenger.s(String.format("Maximum spawn Y value for (%+d, %+d) is %d. You are " + where, x, z, lc)));
        rep.add(Messenger.s("Spawns:"));
        for (EnumCreatureType type : EnumCreatureType.values()) {
            String type_code = type.name()
                .substring(0, 3);
            BiomeGenBase.SpawnListEntry entry = worldIn.spawnRandomCreature(type, x, y, z);
            if (entry == null) continue;
            String creature_name = entry.entityClass.getSimpleName();
            int weight = entry.itemWeight;
            rep.add(
                Messenger.c(
                    String.format(
                        "gi %s: %s (%d:%d-%d), can: ",
                        type_code,
                        creature_name,
                        weight,
                        entry.minGroupCount,
                        entry.maxGroupCount),
                    net.minecraft.world.SpawnerAnimals.canCreatureTypeSpawnAtLocation(type, worldIn, x, y, z) ? "l YES"
                        : "n NO"));
        }
        if (rep.size() == 1) rep.add(Messenger.s(" - Nothing can spawn here."));
        return rep;
    }

    /** Notifies every player about a message, used by debugging commands. */
    public static void notifyPlayers(MinecraftServer server, IChatComponent message) {
        if (server == null) return;
        for (Object o : server.getConfigurationManager().playerEntityList) {
            if (o instanceof EntityPlayer) ((EntityPlayer) o).addChatMessage(message);
        }
    }

    @SuppressWarnings("unused")
    private static void log(String message) {
        Curtain.LOGGER.info(message);
    }

    @SuppressWarnings("unused")
    private static String lower(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
