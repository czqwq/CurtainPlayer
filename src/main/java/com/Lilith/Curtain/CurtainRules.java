package com.Lilith.Curtain;

import static com.Lilith.Curtain.api.rules.Categories.*;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import com.Lilith.Curtain.api.rules.CurtainRule;
import com.Lilith.Curtain.api.rules.IValidator;
import com.Lilith.Curtain.api.rules.Rule;
import com.Lilith.Curtain.api.rules.Validators;
import com.Lilith.Curtain.utils.TranslationHelper;

/**
 * All Curtain rules.
 *
 * <p>
 * 1.7.10 port notes: rules that depend on content which simply does not exist in 1.7.10
 * (shulker boxes, scaffolding, turtle eggs, stripped wood, the end gateway) are not declared here.
 * </p>
 */
@SuppressWarnings("unused")
public class CurtainRules {

    /** While set, block changes performed by the cactus rotator will not produce updates. */
    public static final ThreadLocal<Boolean> impendingFillSkipUpdates = ThreadLocal.withInitial(() -> false);

    public static class LanguageValidator implements IValidator<String> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<String> rule, String newValue) {
            return TranslationHelper.getLanguages()
                .contains(newValue);
        }
    }

    @Rule(categories = FEATURE, validators = LanguageValidator.class, suggestions = { "zh_cn", "en_us" })
    public static String language = "zh_cn";

    public static class ViewDistanceValidator implements IValidator<Integer> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<Integer> rule, String newValue) {
            int value;
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                return false;
            }
            if (value < 0 || value > 32) {
                if (source != null)
                    source.addChatMessage(new ChatComponentText("view distance has to be between 0 and 32"));
                return false;
            }
            MinecraftServer server = Curtain.minecraftServer;
            if (server == null || !server.isDedicatedServer()) {
                if (source != null)
                    source.addChatMessage(new ChatComponentText("view distance can only be changed on a server"));
                return false;
            }
            int vd = (value > 2) ? value
                : server.getConfigurationManager()
                    .getViewDistance();
            if (vd != server.getConfigurationManager()
                .getViewDistance()) {
                server.getConfigurationManager()
                    .func_152611_a(vd);
            }
            return true;
        }
    }

    @Rule(categories = CREATIVE, validators = ViewDistanceValidator.class, suggestions = { "0", "12", "16", "32" })
    public static int viewDistance = 0;

    @Rule(categories = CREATIVE)
    public static boolean xpNoCooldown = false;

    @Rule(categories = COMMAND)
    public static boolean allowSpawningOfflinePlayers = false;

    @Rule(
        categories = COMMAND,
        validators = { Validators.CommandLevel.class },
        suggestions = { "ops", "true", "false" })
    public static String commandPlayer = "ops";

    @Rule(
        categories = COMMAND,
        validators = { Validators.CommandLevel.class },
        suggestions = { "ops", "true", "false" })
    public static String commandLog = "true";

    @Rule(categories = SURVIVAL)
    public static boolean missingTools = false;

    @Rule(categories = { CREATIVE, SURVIVAL, FEATURE })
    public static boolean flippingCactus = false;

    @Rule(categories = { FEATURE })
    public static boolean rotatorBlock = false;

    @Rule(categories = BUGFIX)
    public static boolean placementRotationFix = false;

    @Rule(categories = { CREATIVE })
    public static boolean fillUpdates = false;

    @Rule(categories = { CREATIVE })
    public static boolean interactionUpdates = true;

    @Rule(categories = { CREATIVE, SURVIVAL }, suggestions = { "none", "tps", "mobcaps", "memory", "mobcaps,tps" })
    public static String defaultLoggers = "none";

    @Rule(
        categories = { SURVIVAL },
        suggestions = { "1", "5", "20", "100" },
        serializedName = "hud_logger_update_interval")
    public static int HUDLoggerUpdateInterval = 20;

    @Rule(categories = CREATIVE, suggestions = { "none" }, serializedName = "custom_motd")
    public static String customMOTD = "none";

    @Rule(categories = { CREATIVE, CLIENT })
    public static boolean creativeNoClip = false;

    public static boolean isCreativeFlying(Entity entity) {
        return creativeNoClip && entity instanceof EntityPlayer
            && (((EntityPlayer) entity).capabilities.isCreativeMode)
            && ((EntityPlayer) entity).capabilities.isFlying;
    }

    public static class FakePlayerNameValidator implements IValidator<String> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<String> rule, String newValue) {
            return newValue.matches("^\\w*$");
        }
    }

    @Rule(categories = { COMMAND, BOT }, suggestions = { "none", "bot_" }, validators = FakePlayerNameValidator.class)
    public static String fakePlayerNamePrefix = "none";

    @Rule(categories = { COMMAND, BOT }, suggestions = { "none", "_fake" }, validators = FakePlayerNameValidator.class)
    public static String fakePlayerNameSuffix = "none";

    @Rule(categories = SURVIVAL)
    public static boolean quickLeafDecay = false;

    @Rule(categories = { FEATURE, CLIENT })
    public static boolean superLead = false;

    @Rule(categories = FEATURE)
    public static boolean desertShrubs = false;

    @Rule(categories = CREATIVE)
    public static boolean farmlandTrampledDisabled = false;

    @Rule(categories = { CREATIVE, TNT })
    public static boolean explosionNoBlockDamage = false;

    @Rule(categories = TNT, serializedName = "optimized_tnt")
    public static boolean optimizedTNT = false;

    @Rule(categories = { SURVIVAL, FEATURE })
    public static boolean xpFromExplosions = false;

    public static class CheckOptimizedTntEnabledValidator implements IValidator<Integer> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<Integer> rule, String newValue) {
            boolean b = optimizedTNT || rule.isDefault(newValue);
            if (!b && source != null) {
                source.addChatMessage(new ChatComponentText("optimizedTNT must be enabled"));
            }
            return b;
        }
    }

    public static class TNTRandomRangeValidator implements IValidator<Integer> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<Integer> rule, String newValue) {
            double value;
            try {
                value = Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                return false;
            }
            return value == -1 || value >= 0;
        }
    }

    @Rule(
        categories = TNT,
        suggestions = { "-1" },
        validators = { CheckOptimizedTntEnabledValidator.class, TNTRandomRangeValidator.class })
    public static double tntRandomRange = -1;

    @Rule(categories = { TNT, CREATIVE })
    public static boolean tntPrimerMomentumRemoved = false;

    public static class TNTAngleValidator implements IValidator<Double> {

        @Override
        public boolean validate(ICommandSender source, CurtainRule<Double> rule, String newValue) {
            double value;
            try {
                value = Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                return false;
            }
            boolean b = ((value >= 0 && value < Math.PI * 2) || rule.isDefault(newValue));
            if (!b && source != null) {
                source.addChatMessage(new ChatComponentText("Must be between 0 and 2pi, or -1"));
            }
            return b;
        }
    }

    @Rule(
        categories = TNT,
        suggestions = { "0" },
        validators = TNTAngleValidator.class,
        serializedName = "hardcode_tnt_angle")
    public static double hardcodeTNTAngle = -1.0D;

    @Rule(categories = TNT, serializedName = "merge_tnt")
    public static boolean mergeTNT = false;

    @Rule(categories = { BUGFIX, SURVIVAL })
    public static boolean ctrlQCraftingFix = false;

    @Rule(categories = { CREATIVE, SURVIVAL, BOT })
    public static boolean openFakePlayerInventory = false;

    @Rule(categories = { CREATIVE, SURVIVAL, BOT })
    public static boolean openFakePlayerEnderChest = false;

    @Rule(categories = { CREATIVE, BOT })
    public static boolean fakePlayerAutoFish = false;

    @Rule(categories = { SURVIVAL })
    public static boolean betterSignInteraction = false;

    @Rule(categories = { SURVIVAL })
    public static boolean betterFenceGatePlacement = false;

    @Rule(categories = { CREATIVE, SURVIVAL, BOT })
    public static boolean fakePlayerResident = false;

    @Rule(categories = { CREATIVE, SURVIVAL, BOT })
    public static boolean fakePlayerAutoReplenishment = false;

    @Rule(categories = { CREATIVE, SURVIVAL, BOT })
    public static boolean fakePlayerAutoReplaceTool = false;

    @Rule(categories = { CREATIVE, SURVIVAL })
    public static boolean blockPlacementIgnoreEntity = false;

    @Rule(categories = { FEATURE })
    public static boolean chickenShearing = false;

    @Rule(categories = { CREATIVE, SURVIVAL })
    public static boolean antiCheatDisabled = false;

    /** Convenience: the dedicated server this rule set belongs to, may be null. */
    public static WorldServer overworld() {
        MinecraftServer server = Curtain.minecraftServer;
        return server == null ? null : server.worldServerForDimension(0);
    }
}
