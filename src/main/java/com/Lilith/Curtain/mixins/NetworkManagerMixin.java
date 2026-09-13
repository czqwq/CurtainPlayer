package com.Lilith.Curtain.mixins;

import net.minecraft.network.NetworkManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.Lilith.Curtain.features.player.fakes.IClientConnection;

import io.netty.channel.Channel;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin implements IClientConnection {

    @Shadow
    private Channel channel;

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
