package com.Lilith.Curtain.features.player.patches;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.utils.Messenger;
import com.mojang.authlib.GameProfile;

public class EntityPlayerMPFake extends EntityPlayerMP {

    public boolean isAShadow;
    public Runnable fixStartingPosition = new Runnable() {

        @Override
        public void run() {}
    };

    /**
     * Creates a fake player.
     *
     * @return the fake player, or null when the profile could not be resolved and offline spawning is disabled
     */
    public static EntityPlayerMPFake createFakePlayer(String username, MinecraftServer server, double x, double y,
        double z, double yaw, double pitch, int dimensionId, WorldSettings.GameType gamemode, boolean flying) {
        WorldServer world = server.worldServerForDimension(dimensionId);
        if (world == null) world = server.worldServerForDimension(0);
        GameProfile gameProfile = lookupProfile(server, username);
        if (gameProfile == null) {
            if (!CurtainRules.allowSpawningOfflinePlayers) {
                return null;
            }
            gameProfile = new GameProfile(getOfflineUUID(username), username);
        }
        try {
            EntityPlayerMPFake instance = new EntityPlayerMPFake(server, world, gameProfile, false);
            final double fx = x, fy = y, fz = z;
            final float fyaw = (float) yaw, fpitch = (float) pitch;
            // initializeConnectionToPlayer puts the player at the world spawn and only then adds it to the
            // world, where the entity tracker immediately broadcasts its position. Positioning the player
            // through fixStartingPosition in ServerConfigurationManager#playerLoggedIn makes that first
            // broadcast use the requested coordinates instead of the world spawn.
            instance.fixStartingPosition = new Runnable() {

                @Override
                public void run() {
                    instance.setPositionAndRotation(fx, fy, fz, fyaw, fpitch);
                }
            };
            FakeNetworkManager connection = new FakeNetworkManager();
            NetHandlerPlayServer handler = new NetHandlerPlayServer(server, connection, instance);
            server.getConfigurationManager()
                .initializeConnectionToPlayer(connection, instance, handler);
            instance.fixStartingPosition.run();
            instance.setHealth(instance.getMaxHealth());
            instance.getFoodStats()
                .addStats(20, 20.0F);
            instance.theItemInWorldManager.setGameType(gamemode);
            instance.capabilities.isFlying = flying;
            instance.capabilities.allowFlying = gamemode == WorldSettings.GameType.CREATIVE;
            instance.fallDistance = 0;
            return instance;
        } catch (Exception exception) {
            Messenger.print_server_message(server, "Failed to spawn fake player: " + exception);
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Replaces the given real player with a fake player carrying the same profile and inventory.
     */
    public static EntityPlayerMPFake createShadow(MinecraftServer server, EntityPlayerMP player) {
        player.playerNetServerHandler.onDisconnect(new ChatComponentText("multiplayer.disconnect.duplicate_login"));
        WorldServer world = (WorldServer) player.worldObj;
        GameProfile gameprofile = player.getGameProfile();
        EntityPlayerMPFake playerShadow = new EntityPlayerMPFake(server, world, gameprofile, true);
        final EntityPlayerMP shadowSource = player;
        playerShadow.fixStartingPosition = new Runnable() {

            @Override
            public void run() {
                playerShadow.setPositionAndRotation(
                    shadowSource.posX,
                    shadowSource.posY,
                    shadowSource.posZ,
                    shadowSource.rotationYaw,
                    shadowSource.rotationPitch);
            }
        };
        FakeNetworkManager connection = new FakeNetworkManager();
        NetHandlerPlayServer handler = new NetHandlerPlayServer(server, connection, playerShadow);
        server.getConfigurationManager()
            .initializeConnectionToPlayer(connection, playerShadow, handler);

        playerShadow.setHealth(player.getHealth());
        playerShadow
            .setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        playerShadow.setGameType(player.theItemInWorldManager.getGameType());
        playerShadow.theItemInWorldManager.setGameType(player.theItemInWorldManager.getGameType());
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            playerShadow.inventory.mainInventory[i] = player.inventory.mainInventory[i];
        }
        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            playerShadow.inventory.armorInventory[i] = player.inventory.armorInventory[i];
        }
        playerShadow.inventory.currentItem = player.inventory.currentItem;
        playerShadow.capabilities.isFlying = player.capabilities.isFlying;
        return playerShadow;
    }

    /** The 1.7.10 profile cache is offline, never blocks on the authentication servers. */
    private static GameProfile lookupProfile(MinecraftServer server, String username) {
        try {
            PlayerProfileCache cache = server.func_152358_ax();
            if (cache == null) return null;
            for (String cached : cache.func_152654_a()) {
                if (cached.equalsIgnoreCase(username)) {
                    return cache.func_152655_a(cached);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private EntityPlayerMPFake(MinecraftServer minecraftServer, WorldServer world, GameProfile gameProfile,
        boolean isShadow) {
        super(minecraftServer, world, gameProfile, new net.minecraft.server.management.ItemInWorldManager(world));
        this.isAShadow = isShadow;
    }

    @Override
    public void onUpdate() {
        // packet independent part (chunk sending, container sync, ...)
        try {
            super.onUpdate();
        } catch (NullPointerException ignored) {
            // the fake connection never sends anything, a few vanilla paths assume it does
        }
        // in 1.7.10 the player entity itself is only ticked when the client sends packets
        try {
            this.onUpdateEntity();
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void kill() {
        kill(Messenger.s("Killed"));
    }

    public void kill(IChatComponent reason) {
        shakeOff();
        if (this.playerNetServerHandler != null) {
            this.playerNetServerHandler.onDisconnect(reason);
        }
    }

    private void shakeOff() {
        if (this.ridingEntity instanceof EntityPlayer) {
            this.mountEntity(null);
        }
        if (this.riddenByEntity instanceof EntityPlayer) {
            ((EntityPlayer) this.riddenByEntity).mountEntity(null);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        shakeOff();
        ItemStack[] inventory = this.inventory.mainInventory;
        for (int i = 0; i < inventory.length; i++) {
            this.inventory.mainInventory[i] = null;
        }
        for (int i = 0; i < this.inventory.armorInventory.length; i++) {
            this.inventory.armorInventory[i] = null;
        }
        this.setHealth(this.getMaxHealth());
        this.getFoodStats()
            .addStats(20, 20.0F);
        this.extinguish();
        this.fireResistance = 0;
        kill(new ChatComponentText(this.getCommandSenderName() + " died"));
    }

    @Override
    public String getPlayerIP() {
        return "127.0.0.1";
    }

    @Override
    protected void fall(float distance) {
        // fake players do not take fall damage
    }

    @Override
    public void addChatMessage(IChatComponent message) {
        // nobody is listening
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return false;
    }

    @Override
    public String toString() {
        return "EntityPlayerMPFake[" + this.getCommandSenderName() + "]";
    }
}
