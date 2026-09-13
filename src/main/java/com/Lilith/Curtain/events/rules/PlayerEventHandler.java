package com.Lilith.Curtain.events.rules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.player.helpers.FakePlayerAutoReplaceTool;
import com.Lilith.Curtain.features.player.helpers.FakePlayerAutoReplenishment;
import com.Lilith.Curtain.features.player.menu.MenuHashMap;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;
import com.Lilith.Curtain.utils.CurtainGuiHandler;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerEventHandler {

    // 假人背包(openFakePlayerInventory) / 假人末影箱(openFakePlayerEnderChest)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onInteractWithFakePlayer(EntityInteractEvent event) {
        if (!(event.target instanceof EntityPlayerMPFake)) return;
        EntityPlayerMPFake fakeplayer = (EntityPlayerMPFake) event.target;
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj == null || player.worldObj.isRemote) return;

        boolean enderChest = CurtainRules.openFakePlayerEnderChest && player.isSneaking();
        if (enderChest || CurtainRules.openFakePlayerInventory) {
            MenuHashMap.getOrCreate(fakeplayer);
            MenuHashMap.setViewed(player, fakeplayer);
            player.openGui(
                Curtain.instance,
                enderChest ? CurtainGuiHandler.FAKE_PLAYER_ENDER_CHEST : CurtainGuiHandler.FAKE_PLAYER_INVENTORY,
                player.worldObj,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);
            event.setCanceled(true);
        }
    }

    // 规则存档
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (event.world != null && event.world.provider.dimensionId == 0 && Curtain.rules != null) {
            Curtain.rules.saveToFile();
        }
    }

    // 假人补货(fakePlayerAutoReplenishment)
    public static void onItemUse(EntityPlayer player) {
        if (CurtainRules.fakePlayerAutoReplenishment && player instanceof EntityPlayerMPFake) {
            FakePlayerAutoReplenishment.autoReplenishment(player);
        }
    }

    // 假人换工具(fakePlayerAutoReplaceTool)
    public static void onItemDamaged(EntityPlayer player) {
        if (CurtainRules.fakePlayerAutoReplaceTool && player instanceof EntityPlayerMPFake) {
            FakePlayerAutoReplaceTool.autoReplaceTool(player);
        }
    }
}
