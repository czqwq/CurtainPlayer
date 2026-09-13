package com.Lilith.Curtain.features.player.menu;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;

import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;

public class MenuHashMap {

    /** menus of every fake player, keyed by the fake player */
    public static final HashMap<EntityPlayer, FakePlayerInventoryMenu> FAKE_PLAYER_INVENTORY_MENU_MAP = new HashMap<EntityPlayer, FakePlayerInventoryMenu>();
    /** the fake player a viewer opened the inventory of, keyed by the viewer */
    public static final HashMap<EntityPlayer, EntityPlayer> VIEWED_FAKE_PLAYER = new HashMap<EntityPlayer, EntityPlayer>();

    public static FakePlayerInventoryMenu get(EntityPlayer player) {
        return FAKE_PLAYER_INVENTORY_MENU_MAP.get(player);
    }

    public static FakePlayerInventoryMenu getOrCreate(EntityPlayer player) {
        FakePlayerInventoryMenu menu = FAKE_PLAYER_INVENTORY_MENU_MAP.get(player);
        if (menu == null && player instanceof EntityPlayerMPFake) {
            menu = new FakePlayerInventoryMenu(player);
            FAKE_PLAYER_INVENTORY_MENU_MAP.put(player, menu);
        }
        return menu;
    }

    /** The menu the given viewer wants to see, may be null. */
    public static FakePlayerInventoryMenu getViewed(EntityPlayer viewer) {
        EntityPlayer target = VIEWED_FAKE_PLAYER.get(viewer);
        if (target == null) return null;
        return getOrCreate(target);
    }

    public static void setViewed(EntityPlayer viewer, EntityPlayer fakePlayer) {
        VIEWED_FAKE_PLAYER.put(viewer, fakePlayer);
    }

    public static void clearViewed(EntityPlayer viewer) {
        VIEWED_FAKE_PLAYER.remove(viewer);
    }
}
