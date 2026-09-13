package com.Lilith.Curtain.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.logging.LoggerManager;
import com.Lilith.Curtain.utils.CommandHelper;

public class LogCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "log";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/log <loggerName>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return CommandHelper.canUseCommand(sender, CurtainRules.commandLog);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            this.curtain$processCommand(sender, args);
        } catch (Throwable t) {
            CommandHelper.reportFailure(sender, getCommandName(), t);
        }
    }

    private void curtain$processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("This command can only be used by a player"));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        String playerName = player.getCommandSenderName();
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Loggers: " + String.join(", ", LoggerManager.getLoggerSet())));
            return;
        }
        String loggerName = args[0];
        if (!LoggerManager.getLoggerSet()
            .contains(loggerName)) {
            sender.addChatMessage(new ChatComponentText("Unknown logger: " + loggerName));
            return;
        }
        if (LoggerManager.isSubscribedLogger(playerName, loggerName)) {
            LoggerManager.unsubscribeLogger(playerName, loggerName);
        } else {
            LoggerManager.subscribeLogger(playerName, loggerName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(
                args,
                LoggerManager.getLoggerSet()
                    .toArray(new String[0]));
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @SuppressWarnings("unused")
    private static List<String> empty() {
        return new ArrayList<String>();
    }
}
