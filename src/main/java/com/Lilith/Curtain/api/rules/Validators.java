package com.Lilith.Curtain.api.rules;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;

import com.Lilith.Curtain.utils.CommandHelper;

public final class Validators {

    private Validators() {}

    public static class CommandLevel implements IValidator<String> {

        public static final List<String> OPTIONS = Arrays.asList("true", "false", "ops", "0", "1", "2", "3", "4");

        @Override
        public boolean validate(ICommandSender source, CurtainRule<String> rule, String newValue) {
            boolean is_valid = OPTIONS.contains(newValue);
            if (source != null && is_valid)
                CommandHelper.notifyPlayersCommandsChanged(com.Lilith.Curtain.Curtain.minecraftServer);
            return is_valid;
        }
    }
}
