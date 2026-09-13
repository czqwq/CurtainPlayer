package com.Lilith.Curtain.features.logging.builtin;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.features.logging.AbstractHudLogger;

public class MemoryLogger extends AbstractHudLogger {

    public MemoryLogger() {
        super("memory");
    }

    @Override
    public List<IChatComponent> display(EntityPlayerMP player) {
        long totalMemory = Runtime.getRuntime()
            .totalMemory();
        long freeMemory = Runtime.getRuntime()
            .freeMemory();
        long usedMemory = totalMemory - freeMemory;
        IChatComponent used = new ChatComponentText(String.format("%.1f", usedMemory / 1024 / 1024f) + " M");
        used.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        IChatComponent sep = new ChatComponentText(" / ");
        sep.getChatStyle()
            .setColor(EnumChatFormatting.WHITE);
        IChatComponent total = new ChatComponentText(String.format("%.1f", totalMemory / 1024 / 1024f) + " M");
        total.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        return Collections.singletonList(
            used.appendSibling(sep)
                .appendSibling(total));
    }
}
