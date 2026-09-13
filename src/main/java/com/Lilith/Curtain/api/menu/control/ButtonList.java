package com.Lilith.Curtain.api.menu.control;

import java.util.List;

public abstract class ButtonList {

    protected final List<Button> buttons;

    public ButtonList(List<Button> buttons, final boolean required) {
        this.buttons = buttons;
        if (required) {
            buttons.get(0)
                .turnOnWithoutFunction();
            for (final Button button : this.buttons) {
                button.addTurnOffFunction(() -> {
                    if (this.isAllOff()) {
                        button.turnOnWithoutFunction();
                    }
                });
            }
        }
    }

    public boolean isAllOff() {
        for (Button button : this.buttons) {
            if (button.getFlag()) return false;
        }
        return true;
    }

    public boolean isAllOn() {
        for (Button button : this.buttons) {
            if (!button.getFlag()) return false;
        }
        return true;
    }
}
