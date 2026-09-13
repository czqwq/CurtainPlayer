package com.Lilith.Curtain.api.rules;

import net.minecraft.command.ICommandSender;

@FunctionalInterface
public interface IValidator<T> {

    boolean validate(ICommandSender source, CurtainRule<T> rule, String newValue);
}
