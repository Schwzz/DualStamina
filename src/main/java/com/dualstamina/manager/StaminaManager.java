package com.dualstamina.manager;

import com.dualstamina.data.PlayerStamina;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaminaManager {

    private final Map<UUID, PlayerStamina> staminaMap = new HashMap<>();

    public PlayerStamina getStamina(UUID uuid) {
        return staminaMap.computeIfAbsent(uuid, k -> new PlayerStamina());
    }

    public void removeStamina(UUID uuid) {
        staminaMap.remove(uuid);
    }

    public Map<UUID, PlayerStamina> getStaminaMap() {
        return staminaMap;
    }
}