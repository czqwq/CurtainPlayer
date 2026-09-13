package com.Lilith.Curtain.features.player.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FakePlayerResident {

    public static File getFile(MinecraftServer server) {
        String folder = server == null ? "world" : server.getFolderName();
        return new File(folder, "fake_player.gca.json");
    }

    public static void onServerStop(MinecraftServer server) {
        if (CurtainRules.fakePlayerResident) {
            JsonObject fakePlayerList = new JsonObject();
            for (Object o : server.getConfigurationManager().playerEntityList) {
                if (!(o instanceof EntityPlayerMPFake)) continue;
                EntityPlayerMPFake player = (EntityPlayerMPFake) o;
                fakePlayerList.add(player.getCommandSenderName(), save(player));
            }
            File file = getFile(server);
            try {
                if (!file.isFile()) file.createNewFile();
                try (BufferedWriter bfw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                    bfw.write(new Gson().toJson(fakePlayerList));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void onServerStart(MinecraftServer server) {
        if (!CurtainRules.fakePlayerResident) return;
        File file = getFile(server);
        if (!file.isFile()) return;
        JsonObject fakePlayerList = new JsonObject();
        try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            fakePlayerList = new Gson().fromJson(bfr, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fakePlayerList == null) return;
        for (Map.Entry<String, JsonElement> entry : fakePlayerList.entrySet()) {
            try {
                load(entry, server);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JsonObject save(EntityPlayer player) {
        JsonObject fakePlayer = new JsonObject();
        fakePlayer.addProperty("pos_x", player.posX);
        fakePlayer.addProperty("pos_y", player.posY);
        fakePlayer.addProperty("pos_z", player.posZ);
        fakePlayer.addProperty("yaw", player.rotationYaw);
        fakePlayer.addProperty("pitch", player.rotationPitch);
        fakePlayer.addProperty("dimension", player.dimension);
        WorldSettings.GameType gameType = ((EntityPlayerMP) player).theItemInWorldManager.getGameType();
        // GameType#getName is client only in the MCP mappings, the enum name is stable on both sides
        fakePlayer.addProperty(
            "gamemode",
            gameType.name()
                .toLowerCase(java.util.Locale.ROOT));
        fakePlayer.addProperty("flying", player.capabilities.isFlying);
        return fakePlayer;
    }

    public static void load(Map.Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue()
            .getAsJsonObject();
        double posX = fakePlayer.get("pos_x")
            .getAsDouble();
        double posY = fakePlayer.get("pos_y")
            .getAsDouble();
        double posZ = fakePlayer.get("pos_z")
            .getAsDouble();
        double yaw = fakePlayer.get("yaw")
            .getAsDouble();
        double pitch = fakePlayer.get("pitch")
            .getAsDouble();
        int dimension = fakePlayer.get("dimension")
            .getAsInt();
        String gamemode = fakePlayer.get("gamemode")
            .getAsString();
        boolean flying = fakePlayer.get("flying")
            .getAsBoolean();
        EntityPlayerMPFake.createFakePlayer(
            username,
            server,
            posX,
            posY,
            posZ,
            yaw,
            pitch,
            dimension,
            com.Lilith.Curtain.utils.CommandHelper.parseGameType(gamemode),
            flying);
    }
}
