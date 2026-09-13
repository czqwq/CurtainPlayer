package com.Lilith.Curtain.features.logging;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;

public abstract class AbstractLogger {

    private final String name;
    private final DisplayType type;

    public AbstractLogger(String name, DisplayType type) {
        this.name = name;
        this.type = type;
    }

    public AbstractLogger(String name) {
        this(name, DisplayType.CHAT);
    }

    public String getName() {
        return name;
    }

    public DisplayType getType() {
        return type;
    }

    /**
     * 1.7.10 chat components cannot contain line breaks, so every line is returned separately and sent as its
     * own chat message.
     */
    public abstract List<IChatComponent> display(EntityPlayerMP player);
}
