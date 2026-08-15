package com.dualstamina.command;

import com.dualstamina.DualStaminaPlugin;
import com.dualstamina.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class StaminaCommand implements CommandExecutor, TabCompleter {

    private static final String PERM        = "dualstamina.admin";
    private static final String PREFIX      = "§8[§bStamina§8] §r";
    private static final List<String> KEYS  = Arrays.asList(
            "sprint", "jump", "jump-sneak-mult", "fall",
            "swing", "bow", "shield", "mine",
            "regen-normal", "regen-sneak", "regen-delay"
    );

    private final DualStaminaPlugin plugin;
    private final ConfigManager configManager;

    public StaminaCommand(DualStaminaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(PREFIX + "§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                configManager.load();
                sender.sendMessage(PREFIX + "§aConfiguration reloaded successfully.");
            }

            case "setdrain" -> {
                if (args.length < 3) {
                    sender.sendMessage(PREFIX + "§cUsage: /stamina setdrain <key> <value>");
                    sender.sendMessage(PREFIX + "§7Keys: §f" + String.join("§7, §f", KEYS));
                    return true;
                }
                String key = args[1];
                double value;
                try {
                    value = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + "§c'" + args[2] + "' is not a valid number.");
                    return true;
                }
                if (value < 0) {
                    sender.sendMessage(PREFIX + "§cValue must be >= 0.");
                    return true;
                }
                boolean ok = configManager.setAndSave(key, value);
                if (ok) {
                    sender.sendMessage(PREFIX + "§aSet §e" + key + " §ato §e" + value + "§a. (saved to config.yml)");
                } else {
                    sender.sendMessage(PREFIX + "§cUnknown key '§f" + key + "§c'. Valid keys:");
                    sender.sendMessage(PREFIX + "§7" + String.join("§7, §f", KEYS));
                }
            }

            case "info" -> sendInfo(sender);

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(PREFIX + "§eCommands:");
        sender.sendMessage("  §b/stamina reload §7— Reload config.yml");
        sender.sendMessage("  §b/stamina setdrain <key> <value> §7— Update a drain/regen value");
        sender.sendMessage("  §b/stamina info §7— Show all current drain values");
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(PREFIX + "§eCurrent stamina values:");
        sender.sendMessage("  §7sprint         §f" + configManager.getSprintDrainPerTick() + " /tick");
        sender.sendMessage("  §7jump           §f" + configManager.getJumpDrain());
        sender.sendMessage("  §7jump-sneak-mult§f" + configManager.getJumpSneakMultiplier());
        sender.sendMessage("  §7fall           §f" + configManager.getFallDrainPerHp() + " per HP");
        sender.sendMessage("  §7swing          §f" + configManager.getSwingDrain());
        sender.sendMessage("  §7bow            §f" + configManager.getBowDrain());
        sender.sendMessage("  §7shield         §f" + configManager.getShieldHitDrain());
        sender.sendMessage("  §7mine           §f" + configManager.getMineDrain());
        sender.sendMessage("  §7regen-normal   §f" + configManager.getRegenNormalPerTick() + " /tick");
        sender.sendMessage("  §7regen-sneak    §f" + configManager.getRegenSneakPerTick() + " /tick");
        sender.sendMessage("  §7regen-delay    §f" + configManager.getRegenDelayMs() + " ms");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERM)) return List.of();
        if (args.length == 1) return List.of("reload", "setdrain", "info");
        if (args.length == 2 && args[0].equalsIgnoreCase("setdrain")) return KEYS;
        return List.of();
    }
}