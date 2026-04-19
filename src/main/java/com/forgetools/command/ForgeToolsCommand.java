package com.forgetools.command;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.registry.ToolRegistry;
import com.forgetools.tools.ClaimPolisher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeToolsCommand implements CommandExecutor, TabCompleter {

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;

    private static final List<String> SUBCOMMANDS = Arrays.asList("give", "list", "reload", "setfilter");

    public ForgeToolsCommand(ForgeToolsPlugin plugin, ToolRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "setfilter" -> handleSetFilter(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("forgetools.admin.give")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /forgetools give <player> <tool_id>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        String toolId = args[2].toLowerCase();
        ItemStack item = registry.createToolItem(toolId);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Unknown tool: " + toolId);
            sender.sendMessage(ChatColor.YELLOW + "Available: " + String.join(", ", registry.getAllToolIds()));
            return;
        }
        target.getInventory().addItem(item);
        target.sendMessage(ChatColor.GREEN + "You received: " + registry.getTool(toolId).getDisplayName());
        sender.sendMessage(ChatColor.GREEN + "Given " + toolId + " to " + target.getName() + ".");
    }

    private void handleList(CommandSender sender) {
        Set<String> ids = registry.getAllToolIds();
        sender.sendMessage(ChatColor.GOLD + "=== ForgeTools — " + ids.size() + " tools ===");
        for (String id : ids) {
            String display = registry.getTool(id).getDisplayName();
            sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + id
                    + ChatColor.DARK_GRAY + " → " + display);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("forgetools.admin.reload")) {
            sender.sendMessage(plugin.getLangManager().get("general.no_permission"));
            return;
        }
        plugin.fullReload();
        sender.sendMessage(plugin.getLangManager().get(
                "general.reload_success", "count", registry.getToolCount()));
    }

    private void handleSetFilter(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set filters.");
            return;
        }
        if (!player.hasPermission("forgetools.use.claim_polisher")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /forgetools setfilter <material>");
            return;
        }
        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown material: " + args[1]);
            return;
        }
        if (!material.isBlock()) {
            player.sendMessage(ChatColor.RED + material.name() + " is not a block.");
            return;
        }
        ClaimPolisher polisher = (ClaimPolisher) registry.getTool("claim_polisher");
        if (polisher == null) {
            player.sendMessage(ChatColor.RED + "ClaimPolisher is not loaded.");
            return;
        }
        polisher.setFilter(player.getUniqueId(), material);
        player.sendMessage(ChatColor.GREEN + "Block filter set to " + ChatColor.YELLOW + material.name() + ChatColor.GREEN + ".");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ForgeTools Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/ft give <player> <tool_id>" + ChatColor.GRAY + " — Give a tool");
        sender.sendMessage(ChatColor.YELLOW + "/ft list" + ChatColor.GRAY + " — List all tools");
        sender.sendMessage(ChatColor.YELLOW + "/ft reload" + ChatColor.GRAY + " — Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/ft setfilter <material>" + ChatColor.GRAY + " — Set Claim Polisher filter");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .forEach(completions::add);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .forEach(completions::add);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            registry.getAllToolIds().stream()
                    .filter(id -> id.startsWith(args[2].toLowerCase()))
                    .forEach(completions::add);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setfilter")) {
            Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(Material::name)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .limit(25)
                    .forEach(completions::add);
        }
        return completions;
    }
}
