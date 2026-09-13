package com.Lilith.Curtain.events.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.logging.LoggerManager;
import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.menu.FakePlayerInventoryMenu;
import com.Lilith.Curtain.features.player.menu.MenuHashMap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ServerEventHandler {

    private int timer = 0;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Curtain.minecraftServer == null) return;

        // scheduled plans (fake player fishing, ...)
        WorldServer overworld = Curtain.minecraftServer.worldServerForDimension(0);
        if (overworld != null && Curtain.planExecution != null) {
            Curtain.planExecution.execute(overworld.getTotalWorldTime());
        }

        // action packs of every player
        for (Object o : Curtain.minecraftServer.getConfigurationManager().playerEntityList) {
            if (o instanceof EntityPlayerMP) {
                ((IServerPlayer) o).getActionPack()
                    .onUpdate();
            }
        }

        // fake player inventories
        for (FakePlayerInventoryMenu menu : MenuHashMap.FAKE_PLAYER_INVENTORY_MENU_MAP.values()) {
            menu.tick();
        }

        if (timer <= 0) {
            timer = Math.max(1, CurtainRules.HUDLoggerUpdateInterval);
            LoggerManager.updateHUD();
        }
        timer -= 1;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        String playerName = player.getCommandSenderName();
        if (CurtainRules.defaultLoggers.contentEquals("none")) {
            return;
        }
        if (!LoggerManager.hasSubscribedLogger(playerName)) {
            String[] logs = CurtainRules.defaultLoggers.replace(" ", "")
                .split(",");
            LoggerManager.subscribeLogger(playerName, logs);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LoggerManager.unsubscribeAllLogger(event.player.getCommandSenderName());
        if (event.player instanceof EntityPlayerMP
            && MenuHashMap.FAKE_PLAYER_INVENTORY_MENU_MAP.containsKey(event.player)) {
            MenuHashMap.FAKE_PLAYER_INVENTORY_MENU_MAP.remove(event.player);
        }
    }
}
