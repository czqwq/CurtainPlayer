package com.Lilith.Curtain.commands;

import static com.Lilith.Curtain.utils.TranslationKeys.AS_DEFAULT;
import static com.Lilith.Curtain.utils.TranslationKeys.CHANGE;
import static com.Lilith.Curtain.utils.TranslationKeys.CHANGE_DEFAULT;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.api.rules.Categories;
import com.Lilith.Curtain.api.rules.CurtainRule;
import com.Lilith.Curtain.api.rules.RuleException;
import com.Lilith.Curtain.api.rules.RuleManager;
import com.Lilith.Curtain.utils.MenuHelper;
import com.Lilith.Curtain.utils.Messenger;
import com.Lilith.Curtain.utils.TranslationHelper;

public class RuleCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "curtain";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/curtain [category <name>|setValue <rule> <value>|setDefault <rule> <value>|<rule>]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            this.curtain$processCommand(sender, args);
        } catch (Throwable t) {
            com.Lilith.Curtain.utils.CommandHelper.reportFailure(sender, getCommandName(), t);
        }
    }

    private void curtain$processCommand(ICommandSender sender, String[] args) {
        if (Curtain.rules == null) {
            sender.addChatMessage(new ChatComponentText("Curtain rules are not loaded yet"));
            return;
        }
        if (args.length == 0) {
            Messenger.send(sender, MenuHelper.main());
            return;
        }
        String sub = args[0];
        if ("category".equalsIgnoreCase(sub)) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
                return;
            }
            Messenger.send(sender, MenuHelper.category(args[1]));
            return;
        }
        if ("setValue".equalsIgnoreCase(sub) || "setDefault".equalsIgnoreCase(sub)) {
            boolean setDefault = "setDefault".equalsIgnoreCase(sub);
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
                return;
            }
            String name = args[1];
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) valueBuilder.append(' ');
                valueBuilder.append(args[i]);
            }
            setValue(sender, name, valueBuilder.toString(), setDefault);
            return;
        }
        // convenience: /curtain <rule> -> show the rule
        CurtainRule<?> rule = RuleManager.RULES.get(sub);
        if (rule == null) {
            for (CurtainRule<?> r : RuleManager.RULES.values()) {
                if (r.getName()
                    .equalsIgnoreCase(sub)) {
                    rule = r;
                    break;
                }
            }
        }
        if (rule == null) {
            throw new RuleException("Unknown rule: " + sub);
        }
        sender.addChatMessage(MenuHelper.rule(rule));
    }

    private void setValue(ICommandSender sender, String name, String rawValue, boolean setDefault) {
        CurtainRule<?> rule = RuleManager.RULES.get(name);
        if (null == rule) throw RuleException.nu11();
        if (!rule.validate(sender, rawValue)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid value for " + name));
            return;
        }
        Object obj;
        try {
            obj = rule.parseValue(rawValue);
        } catch (RuntimeException e) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "Not a valid value for " + name + ": " + rawValue));
            return;
        }
        rule.setValue(obj);
        // the rule name is passed as a component so the client can translate it as well
        IChatComponent ruleName = rule.getNameComponent();
        if (setDefault) {
            Curtain.rules.setDefault(rule.getNormalName());
            Curtain.rules.saveToFile();
            IChatComponent msg = TranslationHelper.translate(CHANGE_DEFAULT, ruleName, rawValue);
            msg.getChatStyle()
                .setColor(EnumChatFormatting.GRAY);
            sender.addChatMessage(msg);
        } else {
            IChatComponent msg = TranslationHelper.translate(CHANGE, ruleName, rawValue);
            msg.getChatStyle()
                .setColor(EnumChatFormatting.GRAY);
            msg.appendSibling(new ChatComponentText(" "));
            IChatComponent asDefault = TranslationHelper.translate(AS_DEFAULT);
            asDefault.getChatStyle()
                .setColor(EnumChatFormatting.DARK_GREEN)
                .setChatClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        String.format("/curtain setDefault %s %s", name, rawValue)));
            msg.appendSibling(asDefault);
            sender.addChatMessage(msg);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<String>();
            options.add("category");
            options.add("setValue");
            options.add("setDefault");
            options.addAll(RuleManager.getRuleNames());
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        if (args.length == 2) {
            if ("category".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(
                    args,
                    Categories.getCategories()
                        .toArray(new String[0]));
            }
            if ("setValue".equalsIgnoreCase(args[0]) || "setDefault".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(
                    args,
                    RuleManager.getRuleNames()
                        .toArray(new String[0]));
            }
        }
        if (args.length >= 3 && ("setValue".equalsIgnoreCase(args[0]) || "setDefault".equalsIgnoreCase(args[0]))) {
            CurtainRule<?> rule = RuleManager.RULES.get(args[1]);
            if (rule != null) {
                String[] suggestions = rule.getSuggestions();
                if (suggestions.length > 0) return getListOfStringsMatchingLastWord(args, suggestions);
            }
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}
