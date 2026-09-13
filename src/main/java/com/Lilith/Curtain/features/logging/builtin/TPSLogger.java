package com.Lilith.Curtain.features.logging.builtin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.features.logging.AbstractHudLogger;

public class TPSLogger extends AbstractHudLogger {

    private static final double MAX_TPS = 20d;

    public TPSLogger() {
        super("tps");
    }

    @Override
    public List<IChatComponent> display(EntityPlayerMP player) {
        MinecraftServer server = Curtain.minecraftServer;
        if (server == null) return Collections.emptyList();
        final OptionalDouble averageTPS = Arrays.stream(server.tickTimeArray)
            .average();
        if (!averageTPS.isPresent()) {
            IChatComponent msg = new ChatComponentText("No TPS data available");
            msg.getChatStyle()
                .setColor(EnumChatFormatting.RED);
            return Collections.singletonList(msg);
        }
        double mspt = averageTPS.getAsDouble() * 1.0E-6D;
        double tps = Math.min(1000.0D / mspt, MAX_TPS);
        EnumChatFormatting color = EnumChatFormatting.DARK_GREEN;
        if (mspt >= 20D) color = EnumChatFormatting.GREEN;
        if (mspt >= 35) color = EnumChatFormatting.YELLOW;
        if (mspt >= 45) color = EnumChatFormatting.RED;
        IChatComponent label = new ChatComponentText("TPS: ");
        label.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        IChatComponent tpsText = new ChatComponentText(String.format("%.1f", tps));
        tpsText.getChatStyle()
            .setColor(color);
        IChatComponent label2 = new ChatComponentText(" MSPT: ");
        label2.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        IChatComponent msptText = new ChatComponentText(String.format("%.1f", mspt));
        msptText.getChatStyle()
            .setColor(color);
        return Collections.singletonList(
            label.appendSibling(tpsText)
                .appendSibling(label2)
                .appendSibling(msptText));
    }
}
