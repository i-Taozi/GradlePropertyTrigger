/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.commands;

import java.util.*;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public abstract class SubCommand implements IModCommand {
    public enum PermLevel {
        EVERYONE(0),
        ADMIN(2),
        SERVER_ADMIN(3);
        final int permLevel;

        PermLevel(int permLevel) {
            this.permLevel = permLevel;
        }
    }

    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private PermLevel permLevel = PermLevel.EVERYONE;
    private IModCommand parent;
    private final SortedSet<SubCommand> children = new TreeSet<>(new Comparator<SubCommand>() {

        @Override
        public int compare(SubCommand o1, SubCommand o2) {
            return o1.compareTo(o2);
        }
    });

    public SubCommand(String name) {
        this.name = name;
    }

    @Override
    public final String getCommandName() {
        return name;
    }

    public SubCommand addChildCommand(SubCommand child) {
        child.setParent(this);
        children.add(child);
        return this;
    }

    void setParent(IModCommand parent) {
        this.parent = parent;
    }

    @Override
    public SortedSet<SubCommand> getChildren() {
        return children;
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public final void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!CommandHelpers.processStandardCommands(sender, this, args)) {
            processSubCommand(sender, args);
        }
    }

    public void processSubCommand(ICommandSender sender, String[] args) throws CommandException {
        CommandHelpers.throwWrongUsage(sender, this);
    }

    public SubCommand setPermLevel(PermLevel permLevel) {
        this.permLevel = permLevel;
        return this;
    }

    @Override
    public final int getMinimumPermissionLevel() {
        return permLevel.permLevel;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canCommandSenderUseCommand(getMinimumPermissionLevel(), getCommandName());
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getFullCommandString() + " help";
    }

    @Override
    public void printHelp(ICommandSender sender) {
        CommandHelpers.printHelp(sender, this);
    }

    @Override
    public String getFullCommandString() {
        return parent.getFullCommandString() + " " + getCommandName();
    }

    @Override
    public int compareTo(ICommand command) {
        return this.getCommandName().compareTo(command.getCommandName());
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
}
