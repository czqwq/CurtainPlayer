package com.Lilith.Curtain.api.menu;

import net.minecraft.entity.player.EntityPlayer;

/** Implemented by containers that draw Curtain buttons into their slots. */
public interface IButtonContainer {

    boolean curtain$clickButton(int slotId, EntityPlayer player);
}
