package com.Lilith.Curtain.features.logging.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.features.logging.AbstractLogger;
import com.Lilith.Curtain.features.logging.LoggerManager;
import com.Lilith.Curtain.utils.Messenger;

public class TNTLogHelper {

    public boolean initialized;
    private double primedX, primedY, primedZ;
    private static long lastGametime = 0;
    private static int tntCount = 0;
    private double primedMotionX, primedMotionY, primedMotionZ;
    private static List<IChatComponent> log = new ArrayList<IChatComponent>();

    /**
     * Runs when the TNT is primed. Expects the position and motion angle of the TNT.
     */
    public void onPrimed(double x, double y, double z, double motionX, double motionY, double motionZ) {
        primedX = x;
        primedY = y;
        primedZ = z;
        primedMotionX = motionX;
        primedMotionY = motionY;
        primedMotionZ = motionZ;
        initialized = true;
    }

    /**
     * Runs when the TNT explodes. Expects the position of the TNT.
     */
    public void onExploded(double x, double y, double z, long gametime) {
        if (lastGametime != gametime) {
            tntCount = 0;
            lastGametime = gametime;
        }
        tntCount++;

        log = Collections.singletonList(
            Messenger.c(
                "r #" + tntCount,
                "m @" + gametime,
                "g : ",
                "l P ",
                Messenger.dblf("l", primedX, primedY, primedZ),
                "w  ",
                Messenger.dblf("l", primedMotionX, primedMotionY, primedMotionZ),
                "r  E ",
                Messenger.dblf("r", x, y, z)));
        LoggerManager.ableSendToChat("tnt");
    }

    public static class TNTLogger extends AbstractLogger {

        public TNTLogger() {
            super("tnt");
        }

        @Override
        public List<IChatComponent> display(EntityPlayerMP player) {
            return log;
        }
    }
}
