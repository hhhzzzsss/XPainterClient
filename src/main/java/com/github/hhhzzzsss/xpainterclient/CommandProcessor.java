package com.github.hhhzzzsss.xpainterclient;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.impl.lib.sat4j.tools.xplain.Xplain;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandProcessor {
    public static ArrayList<Command> commands = new ArrayList<>();
    public static HashMap<String, Command> commandMap = new HashMap<>();
    public static ArrayList<String> commandCompletions = new ArrayList<>();

    public static final String PREFIX = "^";

    private static abstract class Command {
        public abstract String getName();
        public abstract String[] getSyntax();
        public abstract String getDescription();
        public abstract boolean processCommand(String args);
        public String[] getAliases() {
            return new String[]{};
        }
        public CompletableFuture<Suggestions> getSuggestions(String args, SuggestionsBuilder suggestionsBuilder) {
            return null;
        }
    }

    public static void initCommands() {
        commands.add(new HelpCommand());
        commands.add(new Pos1Command());
        commands.add(new Pos2Command());
        commands.add(new DeselCommand());
        commands.add(new StopCommand());
        commands.add(new FillCommand());

        for (Command command : commands) {
            commandMap.put(command.getName().toLowerCase(Locale.ROOT), command);
            commandCompletions.add(command.getName());
            for (String alias : command.getAliases()) {
                commandMap.put(alias.toLowerCase(Locale.ROOT), command);
                commandCompletions.add(alias);
            }
        }
    }

    // returns true if it is a command and should be cancelled
    public static boolean processChatMessage(String message) {
        if (message.startsWith(PREFIX)) {
            String[] parts = message.substring(PREFIX.length()).split(" ", 2);
            String name = parts.length>0 ? parts[0] : "";
            String args = parts.length>1 ? parts[1] : "";
            Command c = commandMap.get(name.toLowerCase(Locale.ROOT));
            if (c == null) {
                Util.addChatMessage("§cUnrecognized command");
            } else {
                try {
                    boolean success = c.processCommand(args);
                    if (!success) {
                        if (c.getSyntax().length == 0) {
                            Util.addChatMessage("§cSyntax: " + PREFIX + c.getName());
                        }
                        else if (c.getSyntax().length == 1) {
                            Util.addChatMessage("§cSyntax: " + PREFIX + c.getName() + " " + c.getSyntax()[0]);
                        }
                        else {
                            Util.addChatMessage("§cSyntax:");
                            for (String syntax : c.getSyntax()) {
                                Util.addChatMessage("§c    " + PREFIX + c.getName() + " " + syntax);
                            }
                        }
                    }
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    Util.addChatMessage("§cAn error occurred while running this command: §4" + e.getMessage());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static CompletableFuture<Suggestions> handleSuggestions(String text, SuggestionsBuilder suggestionsBuilder) {
        if (!text.contains(" ")) {
            List<String> names = commandCompletions
                    .stream()
                    .map((commandName) -> PREFIX+commandName)
                    .collect(Collectors.toList());
            return CommandSource.suggestMatching(names, suggestionsBuilder);
        } else {
            String[] split = text.split(" ", 2);
            if (split[0].startsWith(PREFIX)) {
                String commandName = split[0].substring(1).toLowerCase(Locale.ROOT);
                if (commandMap.containsKey(commandName)) {
                    return commandMap.get(commandName).getSuggestions(split.length == 1 ? "" : split[1], suggestionsBuilder);
                }
            }
            return null;
        }
    }

    private static class HelpCommand extends Command {
        public String getName() {
            return "help";
        }

        public String[] getSyntax() {
            return new String[]{"[command]"};
        }

        public String getDescription() {
            return "Lists commands or explains command";
        }

        public boolean processCommand(String args) {
            if (args.length() == 0) {
                StringBuilder helpMessage = new StringBuilder("§aCommands -");
                for (Command c : commands) {
                    helpMessage.append(" " + PREFIX + c.getName());
                }
                Util.addChatMessage(helpMessage.toString());
            } else {
                if (commandMap.containsKey(args.toLowerCase(Locale.ROOT))) {
                    Command c = commandMap.get(args.toLowerCase(Locale.ROOT));
                    Util.addChatMessage("§a------------------------------");
                    Util.addChatMessage("§aHelp: §2" + c.getName());
                    Util.addChatMessage("§aDescription: §2" + c.getDescription());
                    if (c.getSyntax().length == 0) {
                        Util.addChatMessage("§aUsage: §2" + PREFIX + c.getName());
                    } else if (c.getSyntax().length == 1) {
                        Util.addChatMessage("§aUsage: §2" + PREFIX + c.getName() + " " + c.getSyntax()[0]);
                    } else {
                        Util.addChatMessage("§aUsage:");
                        for (String syntax : c.getSyntax()) {
                            Util.addChatMessage("    §2" + PREFIX + c.getName() + " " + syntax);
                        }
                    }
                    if (c.getAliases().length > 0) {
                        Util.addChatMessage("§aAliases: §2" + String.join(", ", c.getAliases()));
                    }
                    Util.addChatMessage("§a------------------------------");
                } else {
                    Util.addChatMessage("§cCommand not recognized: " + args);
                }
            }
            return true;
        }

        public CompletableFuture<Suggestions> getSuggestions(String args, SuggestionsBuilder suggestionsBuilder) {
            return CommandSource.suggestMatching(commandCompletions, suggestionsBuilder);
        }
    }

    private static class Pos1Command extends Command {
        public String getName() {
            return "pos1";
        }
        public String[] getSyntax() {
            return new String[0];
        }
        public String getDescription() {
            return "Selects first pos";
        }
        public boolean processCommand(String args) {
            HitResult hitResult = Util.MC.player.raycast(1000, 0, false);
            if (hitResult == null || !(hitResult instanceof BlockHitResult)) {
                Util.addChatMessage("§cYou must be looking at a block");
                return true;
            }

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            XPainterClient.pos1 = blockHitResult.getBlockPos();
            Util.addChatMessage(String.format("§aSet pos 1 to §2%d %d %d", XPainterClient.pos1.getX(), XPainterClient.pos1.getY(), XPainterClient.pos1.getZ()));

            return true;
        }
    }

    private static class Pos2Command extends Command {
        public String getName() {
            return "pos2";
        }
        public String[] getSyntax() {
            return new String[0];
        }
        public String getDescription() {
            return "Selects second pos";
        }
        public boolean processCommand(String args) {
            HitResult hitResult = Util.MC.player.raycast(1000, 0, false);
            if (hitResult == null || !(hitResult instanceof BlockHitResult)) {
                Util.addChatMessage("§cYou must be looking at a block");
                return true;
            }

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            XPainterClient.pos2 = blockHitResult.getBlockPos();
            Util.addChatMessage(String.format("§aSet pos 2 to §2%d %d %d", XPainterClient.pos2.getX(), XPainterClient.pos2.getY(), XPainterClient.pos2.getZ()));

            return true;
        }
    }

    private static class DeselCommand extends Command {
        public String getName() {
            return "desel";
        }
        public String[] getSyntax() {
            return new String[0];
        }
        public String getDescription() {
            return "Deselects region";
        }
        public boolean processCommand(String args) {
            XPainterClient.pos1 = null;
            XPainterClient.pos2 = null;
            Util.addChatMessage("§aDeselected current region");
            return true;
        }
    }

    private static class StopCommand extends Command {
        public String getName() {
            return "stop";
        }
        public String[] getSyntax() {
            return new String[0];
        }
        public String getDescription() {
            return "Stops any actions";
        }
        public boolean processCommand(String args) {
            XPainterClient.targetFillBlock = null;
            Util.addChatMessage("§aStopped all actions");
            return true;
        }
    }

    private static class FillCommand extends Command {
        public String getName() {
            return "fill";
        }
        public String[] getSyntax() {
            return new String[] {"<block>"};
        }
        public String getDescription() {
            return "Stops any actions";
        }
        public boolean processCommand(String args) {
            if (args.length() == 0) {
                Util.addChatMessage("§cMust specify a block");
                return true;
            }

            try {
                Block block = Registries.BLOCK.get(new Identifier(args));
                XPainterClient.targetFillBlock = block;
                Util.addChatMessage("§aNow filling §2" + Registries.BLOCK.getId(block).getPath());
            } catch (Throwable e) {
                Util.addChatMessage("§cInvalid block");
            }
            return true;
        }
        public CompletableFuture<Suggestions> getSuggestions(String args, SuggestionsBuilder suggestionsBuilder) {
            Stream<String> suggestionStream = Registries.BLOCK.stream().map(block -> Registries.BLOCK.getId(block).getPath());
            return CommandSource.suggestMatching(suggestionStream, suggestionsBuilder);
        }
    }
}
