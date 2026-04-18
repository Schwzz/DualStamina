package com.dualstamina.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStamina {

    private double legStamina = 100.0;
    private double armStamina = 100.0;
    private boolean legExhausted = false;
    private boolean armExhausted = false;
    private long lastLegActivity = 0L;
    private long lastArmActivity = 0L;
    private long adrenalineUntil = 0L;

    public boolean isAdrenalineActive() {
        return System.currentTimeMillis() < adrenalineUntil;
    }

    public void activateAdrenaline(long durationMs) {
        adrenalineUntil = System.currentTimeMillis() + durationMs;
    }

    public double getEffectiveMax(int foodLevel) {
        return foodLevel <= 6 ? 50.0 : 100.0;
    }

    public void drainLeg(double amount, int foodLevel) {
        if (isAdrenalineActive()) return;
        double max = getEffectiveMax(foodLevel);
        legStamina = Math.max(0.0, Math.min(legStamina - amount, max));
        lastLegActivity = System.currentTimeMillis();
    }

    public void drainArm(double amount, int foodLevel) {
        if (isAdrenalineActive()) return;
        double max = getEffectiveMax(foodLevel);
        armStamina = Math.max(0.0, Math.min(armStamina - amount, max));
        lastArmActivity = System.currentTimeMillis();
    }

    public void regenLeg(double amount, int foodLevel) {
        double max = getEffectiveMax(foodLevel);
        legStamina = Math.min(legStamina + amount, max);
    }

    public void regenArm(double amount, int foodLevel) {
        double max = getEffectiveMax(foodLevel);
        armStamina = Math.min(armStamina + amount, max);
    }
}