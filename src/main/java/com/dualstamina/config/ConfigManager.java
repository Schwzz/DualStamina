package com.dualstamina.config;

import com.dualstamina.DualStaminaPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {

    private final DualStaminaPlugin plugin;

    // --- Leg drain ---
    private double sprintDrainPerTick;
    private double jumpDrain;
    private double jumpSneakMultiplier;
    private double fallDrainPerHp;

    // --- Arm drain ---
    private double swingDrain;
    private double bowDrain;
    private double shieldHitDrain;
    private double mineDrain;

    // --- Regen ---
    private long regenDelayMs;
    private double regenNormalPerTick;
    private double regenSneakPerTick;

    public ConfigManager(DualStaminaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        sprintDrainPerTick  = cfg.getDouble("drain.leg.sprint-per-tick",       0.35);
        jumpDrain           = cfg.getDouble("drain.leg.jump",                   8.0);
        jumpSneakMultiplier = cfg.getDouble("drain.leg.jump-sneak-multiplier",  0.5);
        fallDrainPerHp      = cfg.getDouble("drain.leg.fall-per-hp",            2.0);

        swingDrain      = cfg.getDouble("drain.arm.swing",       10.0);
        bowDrain        = cfg.getDouble("drain.arm.bow",         15.0);
        shieldHitDrain  = cfg.getDouble("drain.arm.shield-hit",  12.0);
        mineDrain       = cfg.getDouble("drain.arm.mine",         1.5);

        regenDelayMs        = cfg.getLong  ("regen.delay-ms",         1000L);
        regenNormalPerTick  = cfg.getDouble("regen.normal-per-tick",  0.4);
        regenSneakPerTick   = cfg.getDouble("regen.sneak-per-tick",   0.8);
    }

    /**
     * Writes a drain/regen key back to config.yml on disk, then reloads.
     * Accepted keys: sprint, jump, jump-sneak-mult, fall, swing, bow, shield, mine,
     *                regen-normal, regen-sneak, regen-delay
     *
     * @return true if the key was recognised, false otherwise
     */
    public boolean setAndSave(String key, double value) {
        String path = resolvePath(key);
        if (path == null) return false;

        FileConfiguration cfg = plugin.getConfig();
        cfg.set(path, value);
        plugin.saveConfig();
        load();
        return true;
    }

    private String resolvePath(String key) {
        return switch (key.toLowerCase()) {
            case "sprint"          -> "drain.leg.sprint-per-tick";
            case "jump"            -> "drain.leg.jump";
            case "jump-sneak-mult" -> "drain.leg.jump-sneak-multiplier";
            case "fall"            -> "drain.leg.fall-per-hp";
            case "swing"           -> "drain.arm.swing";
            case "bow"             -> "drain.arm.bow";
            case "shield"          -> "drain.arm.shield-hit";
            case "mine"            -> "drain.arm.mine";
            case "regen-normal"    -> "regen.normal-per-tick";
            case "regen-sneak"     -> "regen.sneak-per-tick";
            case "regen-delay"     -> "regen.delay-ms";
            default                -> null;
        };
    }
}