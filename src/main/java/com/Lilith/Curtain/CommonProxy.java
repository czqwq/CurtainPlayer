package com.Lilith.Curtain;

import com.Lilith.Curtain.commands.LogCommand;
import com.Lilith.Curtain.commands.PlayerCommand;
import com.Lilith.Curtain.commands.RuleCommand;
import com.Lilith.Curtain.utils.CurtainGuiHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(Curtain.instance, new CurtainGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new RuleCommand());
        event.registerServerCommand(new PlayerCommand());
        event.registerServerCommand(new LogCommand());
    }
}
