package com.Lilith.Curtain.features.logging;

public abstract class AbstractHudLogger extends AbstractLogger {

    public AbstractHudLogger(String name) {
        super(name, DisplayType.HUD);
    }
}
