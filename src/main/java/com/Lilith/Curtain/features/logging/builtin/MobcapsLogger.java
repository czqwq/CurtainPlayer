package com.Lilith.Curtain.features.logging.builtin;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import com.Lilith.Curtain.features.logging.AbstractHudLogger;
import com.Lilith.Curtain.utils.SpawnReporter;

public class MobcapsLogger extends AbstractHudLogger {

    public MobcapsLogger() {
        super("mobcaps");
    }

    @Override
    public List<IChatComponent> display(EntityPlayerMP player) {
        WorldServer world = (WorldServer) player.worldObj;
        return SpawnReporter.printMobcapsForDimension(world, false);
    }
}
