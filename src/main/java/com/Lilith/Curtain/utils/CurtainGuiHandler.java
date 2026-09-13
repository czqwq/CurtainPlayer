package com.Lilith.Curtain.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.Lilith.Curtain.features.player.gui.GuiFakePlayerInventory;
import com.Lilith.Curtain.features.player.menu.ContainerFakePlayerEnderChest;
import com.Lilith.Curtain.features.player.menu.ContainerFakePlayerInventory;

import cpw.mods.fml.common.network.IGuiHandler;

public class CurtainGuiHandler implements IGuiHandler {

    public static final int FAKE_PLAYER_INVENTORY = 0;
    public static final int FAKE_PLAYER_ENDER_CHEST = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case FAKE_PLAYER_INVENTORY:
                return new ContainerFakePlayerInventory(player);
            case FAKE_PLAYER_ENDER_CHEST:
                return new ContainerFakePlayerEnderChest(player);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
            case FAKE_PLAYER_INVENTORY:
                return new GuiFakePlayerInventory(player);
            case FAKE_PLAYER_ENDER_CHEST:
                return new GuiFakePlayerInventory(player);
            default:
                return null;
        }
    }
}
