package com.Lilith.Curtain;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.Lilith.Curtain.api.PlanExecution;
import com.Lilith.Curtain.api.rules.RuleManager;
import com.Lilith.Curtain.events.MyEventHandlers;
import com.Lilith.Curtain.features.logging.LoggerManager;
import com.Lilith.Curtain.utils.TranslationHelper;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Curtain.MODID, version = Tags.VERSION, name = "Curtain", acceptedMinecraftVersions = "[1.7.10]")
public class Curtain implements ICurtain {

    public static final String MODID = "curtain";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final List<ICurtain> SUB_MODS = new ArrayList<ICurtain>();
    public static PlanExecution planExecution = null;
    public static RuleManager rules = null;
    public static MinecraftServer minecraftServer = null;
    /** The mod instance, set during preInit. */
    public static Curtain instance = null;

    @SidedProxy(clientSide = "com.Lilith.Curtain.ClientProxy", serverSide = "com.Lilith.Curtain.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        proxy.preInit(event);
        MyEventHandlers.register();
        LoggerManager.registryBuiltinLogger();
        this.addRules(CurtainRules.class);
        this.setTrans();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverAboutToStart(cpw.mods.fml.common.event.FMLServerAboutToStartEvent event) {
        com.Lilith.Curtain.events.utils.ServerLifecycleEventHandler.onServerAboutToStart(event);
    }

    @Mod.EventHandler
    public void serverStarted(cpw.mods.fml.common.event.FMLServerStartedEvent event) {
        com.Lilith.Curtain.events.utils.ServerLifecycleEventHandler.onServerStarted(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopping(cpw.mods.fml.common.event.FMLServerStoppingEvent event) {
        com.Lilith.Curtain.events.utils.ServerLifecycleEventHandler.onServerStopping(event);
    }

    /**
     * 添加窗帘附属
     */
    public static void addSubMod(ICurtain curtain) {
        Curtain.SUB_MODS.add(curtain);
    }

    private void setTrans() {
        InputStream stream;
        stream = TranslationHelper.class.getClassLoader()
            .getResourceAsStream("assets/curtain/lang/zh_cn.json");
        this.parseTrans("zh_cn", stream);
        stream = TranslationHelper.class.getClassLoader()
            .getResourceAsStream("assets/curtain/lang/en_us.json");
        this.parseTrans("en_us", stream);
    }
}
