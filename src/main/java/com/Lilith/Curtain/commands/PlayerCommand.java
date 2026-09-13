package com.Lilith.Curtain.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.util.ForgeDirection;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;
import com.Lilith.Curtain.utils.CommandHelper;
import com.Lilith.Curtain.utils.Messenger;

/**
 * 1.7.10 port of the carpet /player command.
 */
public class PlayerCommand extends CommandBase {

    private static final List<String> ACTION_TYPES = Arrays
        .asList("use", "jump", "attack", "drop", "dropStack", "swapHands");

    @Override
    public String getCommandName() {
        return "player";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/player <name> <spawn|stop|use|jump|attack|drop|dropStack|swapHands|hotbar|kill|shadow|mount|dismount|sneak|unsneak|sprint|unsprint|look|turn|move> ...";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return CommandHelper.canUseCommand(sender, CurtainRules.commandPlayer);
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
        if (args.length < 2) {
            sender.addChatMessage(new net.minecraft.util.ChatComponentText(getCommandUsage(sender)));
            return;
        }
        String playerName = args[0];
        String action = args[1];
        String[] rest = Arrays.copyOfRange(args, 2, args.length);

        if ("spawn".equalsIgnoreCase(action)) {
            spawn(sender, playerName, rest);
            return;
        }
        if ("shadow".equalsIgnoreCase(action)) {
            shadow(sender, playerName);
            return;
        }
        if ("kill".equalsIgnoreCase(action)) {
            if (!cantReMove(sender, playerName)) {
                ((EntityPlayerMPFake) curtain$getPlayerMP(sender, playerName)).kill();
            }
            return;
        }
        if ("stop".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, EntityPlayerActionPack::stopAll);
            return;
        }
        if ("hotbar".equalsIgnoreCase(action)) {
            if (rest.length < 1) {
                sender.addChatMessage(new net.minecraft.util.ChatComponentText("/player <name> hotbar <1-9>"));
                return;
            }
            int slot = curtain$parseInt(sender, rest[0]);
            manipulate(sender, playerName, ap -> ap.setSlot(slot));
            return;
        }
        if ("mount".equalsIgnoreCase(action)) {
            boolean anything = rest.length > 0 && "anything".equalsIgnoreCase(rest[0]);
            manipulate(sender, playerName, ap -> ap.mount(!anything));
            return;
        }
        if ("dismount".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, EntityPlayerActionPack::dismount);
            return;
        }
        if ("sneak".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, ap -> ap.setSneaking(true));
            return;
        }
        if ("unsneak".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, ap -> ap.setSneaking(false));
            return;
        }
        if ("sprint".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, ap -> ap.setSprinting(true));
            return;
        }
        if ("unsprint".equalsIgnoreCase(action)) {
            manipulate(sender, playerName, ap -> ap.setSprinting(false));
            return;
        }
        if ("look".equalsIgnoreCase(action)) {
            look(sender, playerName, rest);
            return;
        }
        if ("turn".equalsIgnoreCase(action)) {
            turn(sender, playerName, rest);
            return;
        }
        if ("move".equalsIgnoreCase(action)) {
            move(sender, playerName, rest);
            return;
        }
        if (ACTION_TYPES.contains(action)) {
            action(sender, playerName, action, rest);
            return;
        }
        sender.addChatMessage(new net.minecraft.util.ChatComponentText(getCommandUsage(sender)));
    }

    private void action(ICommandSender sender, String playerName, String action, String[] rest) {
        EntityPlayerActionPack.ActionType type;
        if ("use".equalsIgnoreCase(action)) type = EntityPlayerActionPack.ActionType.USE;
        else if ("jump".equalsIgnoreCase(action)) type = EntityPlayerActionPack.ActionType.JUMP;
        else if ("attack".equalsIgnoreCase(action)) type = EntityPlayerActionPack.ActionType.ATTACK;
        else if ("drop".equalsIgnoreCase(action)) type = EntityPlayerActionPack.ActionType.DROP_ITEM;
        else if ("dropStack".equalsIgnoreCase(action)) type = EntityPlayerActionPack.ActionType.DROP_STACK;
        else type = EntityPlayerActionPack.ActionType.SWAP_HANDS;

        if ("drop".equalsIgnoreCase(action) || "dropStack".equalsIgnoreCase(action)) {
            if (rest.length >= 1 && !"once".equalsIgnoreCase(rest[0])
                && !"continuous".equalsIgnoreCase(rest[0])
                && !"interval".equalsIgnoreCase(rest[0])) {
                boolean dropAll = "dropStack".equalsIgnoreCase(action);
                String target = rest[0];
                int slot;
                if ("all".equalsIgnoreCase(target)) slot = -2;
                else if ("mainhand".equalsIgnoreCase(target)) slot = -1;
                else if ("offhand".equalsIgnoreCase(target)) {
                    Messenger.m(sender, "r 1.7.10 has no offhand slot");
                    return;
                } else slot = curtain$parseInt(sender, target);
                manipulate(sender, playerName, ap -> ap.drop(slot, dropAll));
                return;
            }
        }

        EntityPlayerActionPack.Action act = EntityPlayerActionPack.Action.once();
        if (rest.length >= 1) {
            if ("continuous".equalsIgnoreCase(rest[0])) {
                act = EntityPlayerActionPack.Action.continuous();
            } else if ("interval".equalsIgnoreCase(rest[0])) {
                int ticks = rest.length >= 2 ? curtain$parseInt(sender, rest[1]) : 1;
                act = EntityPlayerActionPack.Action.interval(Math.max(1, ticks));
            }
        }
        final EntityPlayerActionPack.Action finalAct = act;
        manipulate(sender, playerName, ap -> ap.start(type, finalAct));
    }

    private void look(ICommandSender sender, String playerName, String[] rest) {
        if (rest.length == 0) {
            sender.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    "/player <name> look <north|south|east|west|up|down|at <x> <y> <z>|<yaw> <pitch>>"));
            return;
        }
        String dir = rest[0];
        if ("north".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.NORTH));
        else if ("south".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.SOUTH));
        else if ("east".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.EAST));
        else if ("west".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.WEST));
        else if ("up".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.UP));
        else if ("down".equalsIgnoreCase(dir)) manipulate(sender, playerName, ap -> ap.look(ForgeDirection.DOWN));
        else if ("at".equalsIgnoreCase(dir)) {
            if (rest.length < 4) {
                sender.addChatMessage(new net.minecraft.util.ChatComponentText("/player <name> look at <x> <y> <z>"));
                return;
            }
            double x = curtain$parseDouble(sender, rest[1]);
            double y = curtain$parseDouble(sender, rest[2]);
            double z = curtain$parseDouble(sender, rest[3]);
            manipulate(sender, playerName, ap -> ap.lookAt(net.minecraft.util.Vec3.createVectorHelper(x, y, z)));
        } else if (rest.length >= 2) {
            float yaw = (float) curtain$parseDouble(sender, rest[0]);
            float pitch = (float) curtain$parseDouble(sender, rest[1]);
            manipulate(sender, playerName, ap -> ap.look(yaw, pitch));
        }
    }

    private void turn(ICommandSender sender, String playerName, String[] rest) {
        if (rest.length == 0) {
            sender.addChatMessage(
                new net.minecraft.util.ChatComponentText("/player <name> turn <left|right|back|<yaw> <pitch>>"));
            return;
        }
        if ("left".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.turn(-90, 0));
        else if ("right".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.turn(90, 0));
        else if ("back".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.turn(180, 0));
        else if (rest.length >= 2) {
            float yaw = (float) curtain$parseDouble(sender, rest[0]);
            float pitch = (float) curtain$parseDouble(sender, rest[1]);
            manipulate(sender, playerName, ap -> ap.turn(yaw, pitch));
        }
    }

    private void move(ICommandSender sender, String playerName, String[] rest) {
        if (rest.length == 0) manipulate(sender, playerName, EntityPlayerActionPack::stopMovement);
        else if ("forward".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.setForward(1));
        else if ("backward".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.setForward(-1));
        else if ("left".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.setStrafing(1));
        else if ("right".equalsIgnoreCase(rest[0])) manipulate(sender, playerName, ap -> ap.setStrafing(-1));
    }

    private void spawn(ICommandSender sender, String rawName, String[] rest) {
        MinecraftServer server = com.Lilith.Curtain.Curtain.minecraftServer;
        if (server == null) return;

        String playerName = applyPrefixSuffix(rawName);
        if (server.getConfigurationManager()
            .func_152612_a(playerName) != null) {
            Messenger.m(sender, "r Player ", "rb " + playerName, "r  is already logged on");
            return;
        }
        if (playerName.length() > maxPlayerLength(server)) {
            Messenger.m(sender, "rb Player name: " + playerName + " is too long");
            return;
        }

        double x, y, z;
        float yaw, pitch;
        int dimension = sender.getEntityWorld().provider.dimensionId;
        WorldSettings.GameType mode = WorldSettings.GameType.CREATIVE;
        boolean flying = false;

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            x = player.posX;
            y = player.posY;
            z = player.posZ;
            yaw = player.rotationYaw;
            pitch = player.rotationPitch;
            mode = ((EntityPlayerMP) player).theItemInWorldManager.getGameType();
            flying = player.capabilities.isFlying;
        } else {
            ChunkCoordinates coords = sender.getPlayerCoordinates();
            x = coords.posX;
            y = coords.posY;
            z = coords.posZ;
            yaw = 0;
            pitch = 0;
        }

        for (int i = 0; i < rest.length; i++) {
            String keyword = rest[i];
            if ("at".equalsIgnoreCase(keyword) && i + 3 < rest.length) {
                x = curtain$parseDouble(sender, rest[++i]);
                y = curtain$parseDouble(sender, rest[++i]);
                z = curtain$parseDouble(sender, rest[++i]);
            } else if ("facing".equalsIgnoreCase(keyword) && i + 2 < rest.length) {
                yaw = (float) curtain$parseDouble(sender, rest[++i]);
                pitch = (float) curtain$parseDouble(sender, rest[++i]);
            } else if ("in".equalsIgnoreCase(keyword) && i + 1 < rest.length) {
                String value = rest[++i];
                WorldSettings.GameType gameType = CommandHelper.parseGameType(value);
                if (gameType != null) {
                    mode = gameType;
                } else {
                    int dim = parseDimension(sender, value);
                    dimension = dim;
                }
            }
        }

        if (mode == WorldSettings.GameType.SURVIVAL || mode == WorldSettings.GameType.ADVENTURE) {
            flying = false;
        }

        EntityPlayerMPFake fake = EntityPlayerMPFake
            .createFakePlayer(playerName, server, x, y, z, yaw, pitch, dimension, mode, flying);
        if (fake == null) {
            Messenger.m(
                sender,
                "rb Player " + rawName
                    + " doesn't exist and cannot spawn in online mode. "
                    + "Turn the server offline or enable allowSpawningOfflinePlayers to spawn non-existing players");
        }
    }

    private static int parseDimension(ICommandSender sender, String name) {
        if ("overworld".equalsIgnoreCase(name) || "0".equals(name)) return 0;
        if ("nether".equalsIgnoreCase(name) || "the_nether".equalsIgnoreCase(name) || "-1".equals(name)) return -1;
        if ("end".equalsIgnoreCase(name) || "the_end".equalsIgnoreCase(name) || "1".equals(name)) return 1;
        return curtain$parseInt(sender, name);
    }

    /**
     * Online mode dedicated servers only accept 16 character names. Single player (even when opened to LAN)
     * and offline servers keep the longer limit. FMLCommonHandler is used on purpose: it is not remapped and
     * therefore safe on both the client and the server side.
     */
    private static int maxPlayerLength(MinecraftServer server) {
        return cpw.mods.fml.common.FMLCommonHandler.instance()
            .getSide() == cpw.mods.fml.relauncher.Side.SERVER ? 16 : 40;
    }

    private String applyPrefixSuffix(String playerName) {
        String prefix = "none".equals(CurtainRules.fakePlayerNamePrefix)
            || playerName.startsWith(CurtainRules.fakePlayerNamePrefix) ? "" : CurtainRules.fakePlayerNamePrefix;
        String suffix = "none".equals(CurtainRules.fakePlayerNameSuffix)
            || playerName.endsWith(CurtainRules.fakePlayerNameSuffix) ? "" : CurtainRules.fakePlayerNameSuffix;
        return prefix + playerName + suffix;
    }

    private void shadow(ICommandSender sender, String playerName) {
        EntityPlayerMP player = curtain$getPlayerMP(sender, playerName);
        if (player == null) return;
        if (player instanceof EntityPlayerMPFake) {
            Messenger.m(sender, "r Cannot shadow fake players");
            return;
        }
        EntityPlayerMPFake.createShadow(player.mcServer, player);
    }

    private static EntityPlayerMP curtain$getPlayerMP(ICommandSender sender, String playerName) {
        MinecraftServer server = com.Lilith.Curtain.Curtain.minecraftServer;
        if (server == null) return null;
        return server.getConfigurationManager()
            .func_152612_a(playerName);
    }

    private boolean cantManipulate(ICommandSender sender, String playerName) {
        EntityPlayerMP player = curtain$getPlayerMP(sender, playerName);
        if (player == null) {
            Messenger.m(sender, "r Can only manipulate existing players");
            return true;
        }
        EntityPlayer sendingPlayer = sender instanceof EntityPlayer ? (EntityPlayer) sender : null;
        if (sendingPlayer != null && !sender.canCommandSenderUseCommand(2, "player")
            && sendingPlayer != player
            && !(player instanceof EntityPlayerMPFake)) {
            Messenger.m(sender, "r Non OP players can't control other real players");
            return true;
        }
        return false;
    }

    private boolean cantReMove(ICommandSender sender, String playerName) {
        if (cantManipulate(sender, playerName)) return true;
        EntityPlayerMP player = curtain$getPlayerMP(sender, playerName);
        if (player instanceof EntityPlayerMPFake) return false;
        Messenger.m(sender, "r Only fake players can be moved or killed");
        return true;
    }

    private int manipulate(ICommandSender sender, String playerName, Consumer<EntityPlayerActionPack> action) {
        if (cantManipulate(sender, playerName)) return 0;
        EntityPlayerMP player = curtain$getPlayerMP(sender, playerName);
        action.accept(((IServerPlayer) player).getActionPack());
        return 1;
    }

    private static int curtain$parseInt(ICommandSender sender, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new net.minecraft.command.NumberInvalidException("Not a number: " + value);
        }
    }

    private static double curtain$parseDouble(ICommandSender sender, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new net.minecraft.command.NumberInvalidException("Not a number: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, getPlayers(sender).toArray(new String[0]));
        }
        if (args.length == 2) {
            List<String> options = new ArrayList<String>();
            options.add("spawn");
            options.add("stop");
            options.addAll(ACTION_TYPES);
            options.add("hotbar");
            options.add("kill");
            options.add("shadow");
            options.add("mount");
            options.add("dismount");
            options.add("sneak");
            options.add("unsneak");
            options.add("sprint");
            options.add("unsprint");
            options.add("look");
            options.add("turn");
            options.add("move");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        if (args.length == 3) {
            String action = args[1];
            if ("look".equalsIgnoreCase(action)) {
                return getListOfStringsMatchingLastWord(args, "north", "south", "east", "west", "up", "down", "at");
            }
            if ("turn".equalsIgnoreCase(action)) {
                return getListOfStringsMatchingLastWord(args, "left", "right", "back");
            }
            if ("move".equalsIgnoreCase(action)) {
                return getListOfStringsMatchingLastWord(args, "forward", "backward", "left", "right");
            }
            if (ACTION_TYPES.contains(action)) {
                return getListOfStringsMatchingLastWord(args, "once", "continuous", "interval");
            }
            if ("spawn".equalsIgnoreCase(action)) {
                return getListOfStringsMatchingLastWord(args, "at", "facing", "in");
            }
            if ("mount".equalsIgnoreCase(action)) {
                return getListOfStringsMatchingLastWord(args, "anything");
            }
        }
        if (args.length == 4 && ACTION_TYPES.contains(args[1]) && "interval".equalsIgnoreCase(args[2])) {
            return getListOfStringsMatchingLastWord(args, "1", "5", "20", "100");
        }
        return null;
    }

    private static Collection<String> getPlayers(ICommandSender sender) {
        Set<String> players = new LinkedHashSet<String>();
        players.add("Steve");
        players.add("Alex");
        try {
            String[] names = com.Lilith.Curtain.Curtain.minecraftServer == null ? new String[0]
                : com.Lilith.Curtain.Curtain.minecraftServer.getAllUsernames();
            players.addAll(Arrays.asList(names));
        } catch (RuntimeException ignored) {}
        return players;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @SuppressWarnings("unused")
    private static float unusedClamp(float value) {
        return MathHelper.clamp_float(value, -90.0F, 90.0F);
    }
}
