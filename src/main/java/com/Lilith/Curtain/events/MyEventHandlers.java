package com.Lilith.Curtain.events;

import net.minecraftforge.common.MinecraftForge;

import com.Lilith.Curtain.events.rules.PlayerEventHandler;
import com.Lilith.Curtain.events.utils.ServerEventHandler;

import cpw.mods.fml.common.FMLCommonHandler;

public class MyEventHandlers {

    public static void register() {
        ServerEventHandler server = new ServerEventHandler();
        PlayerEventHandler player = new PlayerEventHandler();

        // Forge events (EntityInteractEvent, WorldEvent.Save, ...)
        MinecraftForge.EVENT_BUS.register(player);

        // FML events (TickEvent, PlayerEvent)
        FMLCommonHandler.instance()
            .bus()
            .register(server);
    }
}
