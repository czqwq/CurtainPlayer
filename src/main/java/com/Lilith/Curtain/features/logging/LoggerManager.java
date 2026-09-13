package com.Lilith.Curtain.features.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.features.logging.builtin.MemoryLogger;
import com.Lilith.Curtain.features.logging.builtin.MobcapsLogger;
import com.Lilith.Curtain.features.logging.builtin.TPSLogger;
import com.Lilith.Curtain.features.logging.helper.ExplosionLogHelper;
import com.Lilith.Curtain.features.logging.helper.TNTLogHelper;

public class LoggerManager {

    private static final Map<String, AbstractLogger> registeredLogger = new HashMap<String, AbstractLogger>();
    // Map<playerName, Set<loggerName>>
    private static final Map<String, Set<String>> subscribedPlayer = new HashMap<String, Set<String>>();
    // last HUD message that was sent to a player, used to avoid spamming unchanged data
    private static final Map<String, String> lastHudMessage = new HashMap<String, String>();

    public static void ableSendToChat(String loggerName) {
        if (!registeredLogger.containsKey(loggerName)) {
            Curtain.LOGGER.error("Can't find logger named: {}", loggerName);
            return;
        }
        AbstractLogger logger = registeredLogger.get(loggerName);
        if (logger.getType() != DisplayType.CHAT) {
            Curtain.LOGGER.error("Logger {} not a chat logger", loggerName);
            return;
        }
        if (Curtain.minecraftServer == null) return;
        for (Map.Entry<String, Set<String>> entry : subscribedPlayer.entrySet()) {
            if (!entry.getValue()
                .contains(loggerName)) {
                continue;
            }
            EntityPlayerMP player = getPlayer(entry.getKey());
            if (player != null) {
                for (IChatComponent msg : logger.display(player)) {
                    if (msg != null) player.addChatMessage(msg);
                }
            }
        }
    }

    /**
     * 1.7.10 has no tab list footer, so HUD loggers are delivered through the chat,
     * only when their content actually changed.
     */
    public static void updateHUD() {
        if (Curtain.minecraftServer == null) return;
        for (Map.Entry<String, Set<String>> entry : subscribedPlayer.entrySet()) {
            EntityPlayerMP player = getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            List<IChatComponent> lines = new ArrayList<IChatComponent>();
            StringBuilder flat = new StringBuilder();
            for (Iterator<String> iterator = entry.getValue()
                .iterator(); iterator.hasNext();) {
                String loggerName = iterator.next();
                AbstractLogger logger = registeredLogger.get(loggerName);
                if (logger != null && logger.getType() == DisplayType.HUD) {
                    for (IChatComponent line : logger.display(player)) {
                        if (line == null) continue;
                        lines.add(line);
                        flat.append(line.getUnformattedText());
                    }
                }
            }
            if (lines.isEmpty()) continue;
            String flatText = flat.toString();
            if (flatText.equals(lastHudMessage.get(entry.getKey()))) continue;
            lastHudMessage.put(entry.getKey(), flatText);
            // one chat message per line: 1.7.10 cannot render line breaks inside a component
            for (IChatComponent line : lines) {
                player.addChatMessage(line);
            }
        }
    }

    private static EntityPlayerMP getPlayer(String name) {
        if (Curtain.minecraftServer == null) return null;
        return Curtain.minecraftServer.getConfigurationManager()
            .func_152612_a(name);
    }

    public static void registerLogger(AbstractLogger logger) {
        registeredLogger.put(logger.getName(), logger);
    }

    public static void subscribeLogger(String playerName, String loggerName) {
        if (!registeredLogger.containsKey(loggerName)) {
            Curtain.LOGGER.error("Can't find logger named: {}", loggerName);
            return;
        }
        Set<String> loggerSet = subscribedPlayer.get(playerName);
        if (loggerSet == null) {
            loggerSet = new HashSet<String>();
            subscribedPlayer.put(playerName, loggerSet);
        }
        loggerSet.add(loggerName);

        EntityPlayerMP player = getPlayer(playerName);
        if (player != null) {
            IChatComponent msg = new ChatComponentText(playerName + " subscribed logger " + loggerName);
            msg.getChatStyle()
                .setColor(EnumChatFormatting.GRAY);
            player.addChatMessage(msg);
        }
    }

    public static void subscribeLogger(String playerName, String[] loggers) {
        Set<String> loggerSet = subscribedPlayer.get(playerName);
        if (loggerSet == null) {
            loggerSet = new HashSet<String>();
            subscribedPlayer.put(playerName, loggerSet);
        }
        loggerSet.addAll(Arrays.asList(loggers));
    }

    public static void unsubscribeLogger(String playerName, String loggerName) {
        if (!registeredLogger.containsKey(loggerName)) {
            Curtain.LOGGER.error("Can't find logger named: {}", loggerName);
            return;
        }
        Set<String> loggerSet = subscribedPlayer.get(playerName);
        if (loggerSet == null) {
            loggerSet = new HashSet<String>();
            subscribedPlayer.put(playerName, loggerSet);
        }
        loggerSet.remove(loggerName);

        EntityPlayerMP player = getPlayer(playerName);
        if (player != null) {
            IChatComponent msg = new ChatComponentText(playerName + " unsubscribed logger " + loggerName);
            msg.getChatStyle()
                .setColor(EnumChatFormatting.GRAY);
            player.addChatMessage(msg);
        }
    }

    public static void unsubscribeAllLogger(String playerName) {
        subscribedPlayer.remove(playerName);
        lastHudMessage.remove(playerName);
    }

    public static boolean isSubscribedLogger(String playerName, String loggerName) {
        if (!registeredLogger.containsKey(loggerName)) {
            Curtain.LOGGER.error("Can't find logger named: {}", loggerName);
            return false;
        }
        Set<String> loggerSet = subscribedPlayer.get(playerName);
        return loggerSet != null && loggerSet.contains(loggerName);
    }

    public static boolean hasSubscribedLogger(String playerName) {
        return subscribedPlayer.containsKey(playerName);
    }

    public static void registryBuiltinLogger() {
        registerLogger(new TPSLogger());
        registerLogger(new MobcapsLogger());
        registerLogger(new MemoryLogger());
        registerLogger(new ExplosionLogHelper.ExplosionLogger());
        registerLogger(new TNTLogHelper.TNTLogger());
    }

    public static Set<String> getLoggerSet() {
        return registeredLogger.keySet();
    }
}
