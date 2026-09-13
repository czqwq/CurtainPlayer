package com.Lilith.Curtain.events.rules.fake_player_auto_fish;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.api.PlanExecution;
import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack.Action;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack.ActionType;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;

public class FishingHookEventHandler {

    public static void onCatching(EntityFishHook hook, EntityPlayer player) {
        if (!CurtainRules.fakePlayerAutoFish) return;
        if (!(player instanceof EntityPlayerMPFake)) return;
        EntityPlayerActionPack ap = ((IServerPlayer) player).getActionPack();
        PlanExecution plans = Curtain.planExecution;
        if (plans == null) return;
        long time = player.worldObj.getTotalWorldTime();
        plans.post(time + 5, time1 -> ap.start(ActionType.USE, Action.once()));
        plans.post(time + 15, time1 -> ap.start(ActionType.USE, Action.once()));
    }
}
