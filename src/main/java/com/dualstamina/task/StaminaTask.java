package com.dualstamina.task;

import com.cryptomorin.xseries.XPotion;
import com.dualstamina.DualStaminaPlugin;
import com.dualstamina.data.PlayerStamina;
import com.dualstamina.manager.StaminaManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class StaminaTask extends BukkitRunnable {

    private static final double SPRINT_DRAIN_PER_TICK = 0.35;
    private static final double REGEN_PER_TICK_NORMAL = 0.4;
    private static final double REGEN_PER_TICK_SNEAK = 0.8;
    private static final long REGEN_DELAY_MS = 1000L;
    private static final double LEG_EXHAUSTION_THRESHOLD = 0.0;
    private static final double LEG_RECOVERY_THRESHOLD = 25.0;
    private static final double ARM_EXHAUSTION_THRESHOLD = 0.0;
    private static final double ARM_RECOVERY_THRESHOLD = 25.0;
    private static final int BARS = 10;
    private static final double CRITICAL_PERCENT = 10.0;

    private final DualStaminaPlugin plugin;
    private final StaminaManager staminaManager;
    private int tickCounter = 0;

    public StaminaTask(DualStaminaPlugin plugin) {
        this.plugin = plugin;
        this.staminaManager = plugin.getStaminaManager();
    }

    @Override
    public void run() {
        tickCounter++;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
            int foodLevel = player.getFoodLevel();
            double effectiveMax = stamina.getEffectiveMax(foodLevel);

            if (foodLevel <= 6) {
                if (stamina.getLegStamina() > 50.0) {
                    stamina.setLegStamina(50.0);
                }
                if (stamina.getArmStamina() > 50.0) {
                    stamina.setArmStamina(50.0);
                }
            }

            if (!stamina.isAdrenalineActive()) {
                handleSprintDrain(player, stamina, foodLevel);
            }

            handleLegExhaustion(player, stamina);
            handleArmExhaustion(player, stamina);
            handleRegen(player, stamina, foodLevel, effectiveMax);

            if (tickCounter % 4 == 0) {
                updateHUD(player, stamina, foodLevel);
            }
        }
    }

    private void handleSprintDrain(Player player, PlayerStamina stamina, int foodLevel) {
        if (player.isSprinting()) {
            if (stamina.getLegStamina() <= 0) {
                player.setSprinting(false);
            } else {
                stamina.drainLeg(SPRINT_DRAIN_PER_TICK, foodLevel);
            }
        }
    }

    private void handleLegExhaustion(Player player, PlayerStamina stamina) {
        if (!stamina.isLegExhausted() && stamina.getLegStamina() <= LEG_EXHAUSTION_THRESHOLD) {
            stamina.setLegExhausted(true);
            player.setSprinting(false);
            applySlownessV(player);
        }

        if (stamina.isLegExhausted()) {
            player.setSprinting(false);
            if (stamina.getLegStamina() >= LEG_RECOVERY_THRESHOLD) {
                stamina.setLegExhausted(false);
                PotionEffectType slownessType = getEffectType("SLOWNESS");
                if (slownessType != null) {
                    player.removePotionEffect(slownessType);
                }
            }
        }
    }

    private void handleArmExhaustion(Player player, PlayerStamina stamina) {
        if (!stamina.isArmExhausted() && stamina.getArmStamina() <= ARM_EXHAUSTION_THRESHOLD) {
            stamina.setArmExhausted(true);
            applyMiningFatigueIII(player);
        }

        if (stamina.isArmExhausted()) {
            if (stamina.getArmStamina() >= ARM_RECOVERY_THRESHOLD) {
                stamina.setArmExhausted(false);
            }
        }
    }

    private void applySlownessV(Player player) {
        XPotion.matchXPotion("SLOWNESS")
                .map(xp -> xp.buildPotionEffect(Integer.MAX_VALUE, 4))
                .ifPresent(player::addPotionEffect);
    }

    private void applyMiningFatigueIII(Player player) {
        XPotion.matchXPotion("MINING_FATIGUE")
                .map(xp -> xp.buildPotionEffect(80, 2))
                .ifPresent(player::addPotionEffect);
    }

    private PotionEffectType getEffectType(String name) {
        return XPotion.matchXPotion(name)
                .map(xp -> xp.buildPotionEffect(1, 1))
                .map(PotionEffect::getType)
                .orElse(null);
    }

    private void handleRegen(Player player, PlayerStamina stamina, int foodLevel, double effectiveMax) {
        long now = System.currentTimeMillis();
        double regenAmount = player.isSneaking() ? REGEN_PER_TICK_SNEAK : REGEN_PER_TICK_NORMAL;

        if (now - stamina.getLastLegActivity() >= REGEN_DELAY_MS) {
            if (stamina.getLegStamina() < effectiveMax) {
                stamina.regenLeg(regenAmount, foodLevel);
            }
        }

        if (now - stamina.getLastArmActivity() >= REGEN_DELAY_MS) {
            if (stamina.getArmStamina() < effectiveMax) {
                stamina.regenArm(regenAmount, foodLevel);
            }
        }
    }

    private void updateHUD(Player player, PlayerStamina stamina, int foodLevel) {
        boolean hungerCapped = foodLevel <= 6;

        String legBarColor = getLegBarColor(stamina, hungerCapped);
        String armBarColor = getArmBarColor(stamina, hungerCapped);

        String legBar = buildBar(stamina.getLegStamina(), legBarColor);
        String armBar = buildBar(stamina.getArmStamina(), armBarColor);

        String message = "§fL: [" + legBar + "§f] §7| §fA: [" + armBar + "§f]";

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    private String getLegBarColor(PlayerStamina stamina, boolean hungerCapped) {
        if (stamina.getLegStamina() < CRITICAL_PERCENT) return "§4";
        return hungerCapped ? "§c" : "§a";
    }

    private String getArmBarColor(PlayerStamina stamina, boolean hungerCapped) {
        if (stamina.getArmStamina() < CRITICAL_PERCENT) return "§4";
        return hungerCapped ? "§c" : "§b";
    }

    private String buildBar(double value, String filledColor) {
        int filled = (int) Math.round((value / 100.0) * BARS);
        filled = Math.max(0, Math.min(BARS, filled));
        StringBuilder sb = new StringBuilder();
        sb.append(filledColor);
        for (int i = 0; i < filled; i++) {
            sb.append("|");
        }
        sb.append("§8");
        for (int i = filled; i < BARS; i++) {
            sb.append("|");
        }
        return sb.toString();
    }
}