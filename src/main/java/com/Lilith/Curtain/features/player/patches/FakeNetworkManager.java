package com.Lilith.Curtain.features.player.patches;

import java.net.SocketAddress;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.features.player.fakes.IClientConnection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * A network connection that throws every packet away. Used to give fake players a working
 * {@code NetHandlerPlayServer} without an actual client on the other end.
 */
public class FakeNetworkManager extends NetworkManager {

    public FakeNetworkManager() {
        super(false);
        // this netty version refuses an EmbeddedChannel without any handler
        ((IClientConnection) this).setChannel(new EmbeddedChannel(new io.netty.channel.ChannelInboundHandlerAdapter()));
    }

    @Override
    public void scheduleOutboundPacket(Packet packet, GenericFutureListener... listeners) {
        // drop
    }

    @Override
    public void processReceivedPackets() {
        // drop
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        // drop
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // drop
    }

    @Override
    public void closeChannel(IChatComponent message) {
        // keep the player alive until the disconnect is handled explicitly
    }

    @Override
    public SocketAddress getSocketAddress() {
        return null;
    }

    @Override
    public boolean isChannelOpen() {
        return true;
    }

    @Override
    public boolean isLocalChannel() {
        return true;
    }
}
