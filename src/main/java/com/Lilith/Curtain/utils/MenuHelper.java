package com.Lilith.Curtain.utils;

import static com.Lilith.Curtain.utils.TranslationKeys.CATEGORIES;
import static com.Lilith.Curtain.utils.TranslationKeys.MENU_CATEGORIES;
import static com.Lilith.Curtain.utils.TranslationKeys.MENU_CATEGORY;
import static com.Lilith.Curtain.utils.TranslationKeys.MENU_TITLE;
import static com.Lilith.Curtain.utils.TranslationKeys.MENU_VERSION;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.Curtain;
import com.Lilith.Curtain.Tags;
import com.Lilith.Curtain.api.rules.CurtainRule;
import com.Lilith.Curtain.api.rules.RuleManager;

/**
 * The chat menu. 1.7.10 cannot render line breaks inside a chat component, so every line of the menu is
 * returned as its own component and sent as a separate chat message.
 */
public class MenuHelper {

    /** 主菜单 */
    public static List<IChatComponent> main() {
        List<IChatComponent> lines = new ArrayList<IChatComponent>();
        IChatComponent title = TranslationHelper.translate(MENU_TITLE);
        title.getChatStyle()
            .setBold(true);
        lines.add(title);
        for (CurtainRule<?> rule : Curtain.rules.getSortedRules()) {
            lines.add(rule(rule));
        }
        IChatComponent version = TranslationHelper.translate(MENU_VERSION, Tags.VERSION);
        version.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        lines.add(version);

        IChatComponent categories = TranslationHelper.translate(MENU_CATEGORIES);
        categories.getChatStyle()
            .setBold(true);
        for (String s : RuleManager.getAllCategoriesOrdered()) {
            if (!RuleManager.CATEGORIES_RULES.containsKey(s)) continue;
            ChatComponentText category = new ChatComponentText("");
            category.appendSibling(new ChatComponentText("["));
            category.appendSibling(TranslationHelper.translate(String.format(CATEGORIES, Curtain.MODID, s)));
            category.appendSibling(new ChatComponentText("] "));
            category.getChatStyle()
                .setColor(EnumChatFormatting.AQUA)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/curtain category " + s));
            categories.appendSibling(category);
        }
        lines.add(categories);
        return lines;
    }

    /** 分类 */
    public static List<IChatComponent> category(String name) {
        List<IChatComponent> lines = new ArrayList<IChatComponent>();
        IChatComponent display = new ChatComponentText(name);
        if (RuleManager.CATEGORIES_RULES.containsKey(name)) {
            display = TranslationHelper.translate(String.format(CATEGORIES, Curtain.MODID, name));
        }
        IChatComponent header = TranslationHelper.translate(MENU_CATEGORY, display);
        header.getChatStyle()
            .setBold(true);
        lines.add(header);
        for (String rule : RuleManager.getRuleNamesOfCategory(name)) {
            lines.add(rule(RuleManager.RULES.get(rule)));
        }
        return lines;
    }

    /** 规则 */
    public static IChatComponent rule(CurtainRule<?> rule) {
        ChatComponentText main = new ChatComponentText("");
        IChatComponent name = new ChatComponentText("");
        name.appendSibling(rule.getNameComponent());
        name.appendSibling(new ChatComponentText(String.format("(%s): ", rule.getNormalName())));
        name.getChatStyle()
            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, rule.getDescComponent()));
        String value = String.valueOf(rule.getValue());
        main.appendSibling(name);
        ChatComponentText current = new ChatComponentText(String.format("[%s]", value));
        current.getChatStyle()
            .setColor(rule.isDefault(value) ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.YELLOW)
            .setUnderlined(true)
            .setChatClickEvent(
                new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    String.format(
                        "/curtain setValue %s %s",
                        rule.getNormalName(),
                        rule.getType() == String.class ? "\"" + value + "\"" : value)))
            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("单击来快速填充")));
        main.appendSibling(current);
        for (String s : rule.getSuggestions()) {
            if (replaceQuotation(s).equals(value)) continue;
            main.appendSibling(new ChatComponentText(" "));
            ChatComponentText suggestion = new ChatComponentText(String.format("[%s]", replaceQuotation(s)));
            suggestion.getChatStyle()
                .setColor(
                    rule.isDefault(replaceQuotation(s)) ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.YELLOW)
                .setChatClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        String.format("/curtain setValue %s %s", rule.getNormalName(), s)))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("单击来快速填充")));
            main.appendSibling(suggestion);
        }
        return main;
    }

    private static String replaceQuotation(String s) {
        return s.replace("\"", "");
    }
}
