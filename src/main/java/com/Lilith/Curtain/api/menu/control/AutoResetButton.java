package com.Lilith.Curtain.api.menu.control;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.utils.TranslationHelper;

public class AutoResetButton extends Button {

    public AutoResetButton(String key) {
        super(false, text(key), text(key));
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    private static IChatComponent text(String key) {
        return new ChatComponentText(TranslationHelper.translateLiteral(key))
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE));
    }
}
