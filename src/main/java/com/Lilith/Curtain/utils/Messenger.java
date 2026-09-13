package com.Lilith.Curtain.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Messenger {

    public static final Logger LOG = LogManager.getLogger("Messaging System");

    public enum CarpetFormatting {

        ITALIC('i', (s, f) -> s.setItalic(true)),
        STRIKE('s', (s, f) -> s.setStrikethrough(true)),
        UNDERLINE('u', (s, f) -> s.setUnderlined(true)),
        BOLD('b', (s, f) -> s.setBold(true)),
        OBFUSCATE('o', (s, f) -> s.setObfuscated(true)),

        WHITE('w', (s, f) -> s.setColor(EnumChatFormatting.WHITE)),
        YELLOW('y', (s, f) -> s.setColor(EnumChatFormatting.YELLOW)),
        LIGHT_PURPLE('m', (s, f) -> s.setColor(EnumChatFormatting.LIGHT_PURPLE)),
        RED('r', (s, f) -> s.setColor(EnumChatFormatting.RED)),
        AQUA('c', (s, f) -> s.setColor(EnumChatFormatting.AQUA)),
        GREEN('l', (s, f) -> s.setColor(EnumChatFormatting.GREEN)),
        BLUE('t', (s, f) -> s.setColor(EnumChatFormatting.BLUE)),
        DARK_GRAY('f', (s, f) -> s.setColor(EnumChatFormatting.DARK_GRAY)),
        GRAY('g', (s, f) -> s.setColor(EnumChatFormatting.GRAY)),
        GOLD('d', (s, f) -> s.setColor(EnumChatFormatting.GOLD)),
        DARK_PURPLE('p', (s, f) -> s.setColor(EnumChatFormatting.DARK_PURPLE)),
        DARK_RED('n', (s, f) -> s.setColor(EnumChatFormatting.DARK_RED)),
        DARK_AQUA('q', (s, f) -> s.setColor(EnumChatFormatting.DARK_AQUA)),
        DARK_GREEN('e', (s, f) -> s.setColor(EnumChatFormatting.DARK_GREEN)),
        DARK_BLUE('v', (s, f) -> s.setColor(EnumChatFormatting.DARK_BLUE)),
        BLACK('k', (s, f) -> s.setColor(EnumChatFormatting.BLACK));

        public final char code;
        public final BiFunction<ChatStyle, String, ChatStyle> applier;

        CarpetFormatting(char code, BiFunction<ChatStyle, String, ChatStyle> applier) {
            this.code = code;
            this.applier = applier;
        }

        public ChatStyle apply(String format, ChatStyle previous) {
            if (format.indexOf(this.code) >= 0) return applier.apply(previous, Character.toString(this.code));
            return previous;
        }
    }

    public static ChatStyle parseStyle(String style) {
        ChatStyle myStyle = new ChatStyle().setColor(EnumChatFormatting.WHITE);
        for (CarpetFormatting cf : CarpetFormatting.values()) myStyle = cf.apply(style, myStyle);
        return myStyle;
    }

    public static String heatmap_color(double actual, double reference) {
        String color = "g";
        if (actual >= 0.0D) color = "e";
        if (actual > 0.5D * reference) color = "y";
        if (actual > 0.8D * reference) color = "r";
        if (actual > reference) color = "m";
        return color;
    }

    public static String creatureTypeColor(EnumCreatureType type) {
        if (type == EnumCreatureType.monster) return "n";
        if (type == EnumCreatureType.creature) return "e";
        if (type == EnumCreatureType.ambient) return "f";
        if (type == EnumCreatureType.waterCreature) return "v";
        return "w";
    }

    private static IChatComponent getChatComponentFromDesc(String message, IChatComponent previousMessage) {
        if (message.equalsIgnoreCase("")) {
            return new ChatComponentText("");
        }
        if (Character.isWhitespace(message.charAt(0))) {
            message = "w" + message;
        }
        int limit = message.indexOf(' ');
        String desc = message;
        String str = "";
        if (limit >= 0) {
            desc = message.substring(0, limit);
            str = message.substring(limit + 1);
        }
        if (previousMessage == null) {
            ChatComponentText text = new ChatComponentText(str);
            text.setChatStyle(parseStyle(desc));
            return text;
        }
        ChatStyle previousStyle = previousMessage.getChatStyle();
        switch (desc.charAt(0)) {
            case '?':
                previousStyle
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
                return previousMessage;
            case '!':
                previousStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
                return previousMessage;
            case '^':
                previousStyle.setChatHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(message.substring(1))));
                return previousMessage;
            case '@':
                previousStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, message.substring(1)));
                return previousMessage;
            default: {
                ChatComponentText ret = new ChatComponentText(str);
                ret.setChatStyle(parseStyle(desc));
                return ret;
            }
        }
    }

    public static IChatComponent tp(String desc, double x, double y, double z) {
        return tp(desc, (float) x, (float) y, (float) z);
    }

    public static IChatComponent tp(String desc, float x, float y, float z) {
        return getCoordsTextComponent(desc, x, y, z, false);
    }

    public static IChatComponent tp(String desc, int x, int y, int z) {
        return getCoordsTextComponent(desc, (float) x, (float) y, (float) z, true);
    }

    public static IChatComponent dbl(String style, double double_value) {
        return c(String.format("%s %.1f", style, double_value), String.format("^w %f", double_value));
    }

    public static IChatComponent dbls(String style, double... doubles) {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles) {
            str.append(String.format("%s%.1f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }

    public static IChatComponent dblf(String style, double... doubles) {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles) {
            str.append(String.format("%s%f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }

    public static IChatComponent dblt(String style, double... doubles) {
        List<Object> components = new ArrayList<Object>();
        components.add(style + " [ ");
        String prefix = "";
        for (double dbl : doubles) {
            components.add(String.format("%s %s%.1f", style, prefix, dbl));
            components.add("?" + dbl);
            components.add("^w " + dbl);
            prefix = ", ";
        }
        components.add(style + "  ]");
        return c(components.toArray(new Object[0]));
    }

    private static IChatComponent getCoordsTextComponent(String style, float x, float y, float z, boolean isInt) {
        String text;
        String command;
        if (isInt) {
            text = String.format("%s [ %d, %d, %d ]", style, (int) x, (int) y, (int) z);
            command = String.format("!/tp %d %d %d", (int) x, (int) y, (int) z);
        } else {
            text = String.format("%s [ %.1f, %.1f, %.1f]", style, x, y, z);
            command = String.format("!/tp %.3f %.3f %.3f", x, y, z);
        }
        return c(text, command);
    }

    // message source
    public static void m(ICommandSender source, Object... fields) {
        if (source != null) source.addChatMessage(c(fields));
    }

    public static void m(EntityPlayer player, Object... fields) {
        player.addChatMessage(c(fields));
    }

    /**
     * composes single line, multicomponent message, and returns as one chat message
     */
    public static IChatComponent c(Object... fields) {
        ChatComponentText message = new ChatComponentText("");
        IChatComponent previousComponent = null;
        for (Object o : fields) {
            if (o instanceof IChatComponent) {
                message.appendSibling((IChatComponent) o);
                previousComponent = (IChatComponent) o;
                continue;
            }
            String txt = o == null ? "null" : o.toString();
            IChatComponent comp = getChatComponentFromDesc(txt, previousComponent);
            if (comp != previousComponent) message.appendSibling(comp);
            previousComponent = comp;
        }
        return message;
    }

    // simple text
    public static IChatComponent s(String text) {
        return s(text, "");
    }

    public static IChatComponent s(String text, String style) {
        ChatComponentText message = new ChatComponentText(text);
        message.setChatStyle(parseStyle(style));
        return message;
    }

    public static void send(EntityPlayer player, Collection<IChatComponent> lines) {
        for (IChatComponent message : lines) player.addChatMessage(message);
    }

    public static void send(ICommandSender source, Collection<IChatComponent> lines) {
        for (IChatComponent line : lines) source.addChatMessage(line);
    }

    public static void print_server_message(MinecraftServer server, String message) {
        if (server == null) {
            LOG.error("Message not delivered: " + message);
            return;
        }
        server.getConfigurationManager()
            .sendChatMsg(c("gi " + message));
    }

    public static void print_server_message(MinecraftServer server, IChatComponent message) {
        if (server == null) {
            LOG.error("Message not delivered: " + message.getUnformattedText());
            return;
        }
        server.getConfigurationManager()
            .sendChatMsg(message);
    }
}
