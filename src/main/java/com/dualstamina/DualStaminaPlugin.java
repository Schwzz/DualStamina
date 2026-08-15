package com.dualstamina;

import com.dualstamina.command.StaminaCommand;
import com.dualstamina.config.ConfigManager;
import com.dualstamina.manager.StaminaManager;
import com.dualstamina.task.StaminaTask;
import com.dualstamina.listener.StaminaListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class DualStaminaPlugin extends JavaPlugin {

    @Getter
    private StaminaManager staminaManager;
    @Getter
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.staminaManager = new StaminaManager();

        getServer().getPluginManager().registerEvents(new StaminaListener(this), this);
        new StaminaTask(this).runTaskTimer(this, 0L, 1L);

        StaminaCommand cmd = new StaminaCommand(this);
        getCommand("stamina").setExecutor(cmd);
        getCommand("stamina").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
    }
}