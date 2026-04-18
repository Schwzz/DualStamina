package com.dualstamina;

import com.dualstamina.manager.StaminaManager;
import com.dualstamina.task.StaminaTask;
import com.dualstamina.listener.StaminaListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class DualStaminaPlugin extends JavaPlugin {

    @Getter
    private StaminaManager staminaManager;

    @Override
    public void onEnable() {
        this.staminaManager = new StaminaManager();
        getServer().getPluginManager().registerEvents(new StaminaListener(this), this);
        new StaminaTask(this).runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
    }
}