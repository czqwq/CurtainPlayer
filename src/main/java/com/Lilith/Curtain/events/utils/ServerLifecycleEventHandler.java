package com.Lilith.Curtain.events.utils;

import net.minecraft.server.MinecraftServer;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.api.PlanExecution;
import com.Lilith.Curtain.api.rules.RuleManager;
import com.Lilith.Curtain.features.player.helpers.FakePlayerResident;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

/**
 * 1.7.10 dispatches the FML server lifecycle events to {@code @Mod.EventHandler} methods of the mod
 * class, so these are plain methods called from {@link Curtain}.
 */
public final class ServerLifecycleEventHandler {

    private ServerLifecycleEventHandler() {}

    public static void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        Curtain.minecraftServer = event.getServer();
        Curtain.rules = new RuleManager(event.getServer(), Curtain.MODID);
        Curtain.planExecution = new PlanExecution();
    }

    public static void onServerStarted(cpw.mods.fml.common.event.FMLServerStartedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance()
            .getMinecraftServerInstance();
        if (server != null) {
            FakePlayerResident.onServerStart(server);
        }
    }

    public static void onServerStopping(FMLServerStoppingEvent event) {
        if (Curtain.rules != null) {
            Curtain.rules.saveToFile();
        }
        MinecraftServer server = FMLCommonHandler.instance()
            .getMinecraftServerInstance();
        if (server != null) {
            FakePlayerResident.onServerStop(server);
        }
        Curtain.minecraftServer = null;
    }
}
