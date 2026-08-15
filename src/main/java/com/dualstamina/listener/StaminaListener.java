package com.dualstamina.listener;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.dualstamina.DualStaminaPlugin;
import com.dualstamina.config.ConfigManager;
import com.dualstamina.data.PlayerStamina;
import com.dualstamina.manager.StaminaManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class StaminaListener implements Listener {

    private static final int    SHIELD_COOLDOWN_TICKS  = 100;
    private static final long   ADRENALINE_DURATION_MS = 10_000L;

    private final DualStaminaPlugin plugin;
    private final StaminaManager staminaManager;
    private final ConfigManager configManager;

    public StaminaListener(DualStaminaPlugin plugin) {
        this.plugin = plugin;
        this.staminaManager = plugin.getStaminaManager();
        this.configManager  = plugin.getConfigManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        staminaManager.getStamina(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        staminaManager.removeStamina(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnGround() && event.getFrom().getY() < event.getTo().getY()) {
            double deltaY = event.getTo().getY() - event.getFrom().getY();
            if (deltaY > 0.4) {
                PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
                double baseCost = configManager.getJumpDrain();
                double cost = player.isSneaking()
                        ? baseCost * configManager.getJumpSneakMultiplier()
                        : baseCost;
                stamina.drainLeg(cost, player.getFoodLevel());
            }
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
        stamina.drainArm(configManager.getMineDrain(), player.getFoodLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        double damage = event.getFinalDamage();
        if (damage <= 0) return;
        PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
        stamina.drainLeg(damage * configManager.getFallDrainPerHp(), player.getFoodLevel());
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        XMaterial xMat = XMaterial.matchXMaterial(item.getType());
        String name = xMat.name();
        if (name.endsWith("_SWORD") || name.endsWith("_AXE") || name.equals("MACE")) {
            PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
            stamina.drainArm(configManager.getSwingDrain(), player.getFoodLevel());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
        stamina.drainArm(configManager.getBowDrain(), player.getFoodLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!defender.isBlocking()) return;

        PlayerStamina stamina = staminaManager.getStamina(defender.getUniqueId());
        stamina.drainArm(configManager.getShieldHitDrain(), defender.getFoodLevel());

        if (stamina.getArmStamina() <= 0) {
            Optional<Material> shieldMat = XMaterial.matchXMaterial("SHIELD").map(XMaterial::parseMaterial);
            shieldMat.ifPresent(mat -> defender.setCooldown(mat, SHIELD_COOLDOWN_TICKS));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        XMaterial xMat = XMaterial.matchXMaterial(item.getType());
        String name = xMat.name();

        boolean isGoldenApple = name.equals("GOLDEN_APPLE") || name.equals("ENCHANTED_GOLDEN_APPLE");
        boolean isSpeedPotion = false;

        if (name.equals("POTION") || name.equals("SPLASH_POTION") || name.equals("LINGERING_POTION")) {
            if (item.getItemMeta() instanceof PotionMeta meta) {
                PotionEffectType speedType = XPotion.matchXPotion("SPEED")
                        .map(xp -> xp.buildPotionEffect(1, 1))
                        .map(pe -> pe.getType())
                        .orElse(null);
                if (speedType != null) {
                    boolean hasSpeed = meta.getCustomEffects().stream()
                            .anyMatch(pe -> pe.getType().equals(speedType));
                    if (!hasSpeed && meta.getBasePotionType() != null) {
                        hasSpeed = meta.getBasePotionType().getPotionEffects().stream()
                                .anyMatch(pe -> pe.getType().equals(speedType));
                    }
                    isSpeedPotion = hasSpeed;
                }
            }
        }

        if (isGoldenApple || isSpeedPotion) {
            PlayerStamina stamina = staminaManager.getStamina(player.getUniqueId());
            stamina.activateAdrenaline(ADRENALINE_DURATION_MS);
        }
    }
}