package com.Lilith.Curtain.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * A few helpful methods to work with settings and commands.
 */
public final class CommandHelper {

    private CommandHelper() {}

    /**
     * 1.7.10 asks the server for tab completions, so there is no command tree to resend.
     */
    public static void notifyPlayersCommandsChanged(MinecraftServer server) {}

    /**
     * Parses a game mode name without WorldSettings.GameType#getByName, which is client only in the MCP
     * mappings. The enum constants are not renamed by the obfuscator, so they are safe on both sides.
     */
    public static net.minecraft.world.WorldSettings.GameType parseGameType(String name) {
        if (name == null) return null;
        if (name.equalsIgnoreCase("survival") || name.equalsIgnoreCase("s")) {
            return net.minecraft.world.WorldSettings.GameType.SURVIVAL;
        }
        if (name.equalsIgnoreCase("creative") || name.equalsIgnoreCase("c")) {
            return net.minecraft.world.WorldSettings.GameType.CREATIVE;
        }
        if (name.equalsIgnoreCase("adventure") || name.equalsIgnoreCase("a")) {
            return net.minecraft.world.WorldSettings.GameType.ADVENTURE;
        }
        for (net.minecraft.world.WorldSettings.GameType type : net.minecraft.world.WorldSettings.GameType.values()) {
            if (type.name()
                .equalsIgnoreCase(name)) return type;
        }
        return null;
    }

    /**
     * 1.7.10 prints command exceptions to the console and leaves the player without any feedback, this makes sure
     * the sender at least gets a readable message.
     */
    public static void reportFailure(ICommandSender sender, String command, Throwable t) {
        com.Lilith.Curtain.Curtain.LOGGER.error("Command /" + command + " failed", t);
        if (sender != null) {
            sender.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    net.minecraft.util.EnumChatFormatting.RED + "/"
                        + command
                        + " failed: "
                        + t.getClass()
                            .getSimpleName()
                        + (t.getMessage() == null ? "" : ": " + t.getMessage())));
        }
    }

    /**
     * Whether the given source has enough permission level to run a command that requires the given commandLevel
     */
    public static boolean canUseCommand(ICommandSender source, Object commandLevel) {
        if (source == null) return false;
        if (commandLevel instanceof Boolean) return (Boolean) commandLevel;
        String commandLevelString = String.valueOf(commandLevel);
        if ("true".equals(commandLevelString)) return true;
        if ("ops".equals(commandLevelString)) return source.canCommandSenderUseCommand(2, "curtain");
        if (commandLevelString.length() == 1 && Character.isDigit(commandLevelString.charAt(0))) {
            return source.canCommandSenderUseCommand(Integer.parseInt(commandLevelString), "curtain");
        }
        return false;
    }
}
