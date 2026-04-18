# ⚡ HardcoreStamina: Tactical Exertion

[![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-gold.svg)](https://papermc.io/)
[![Version](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

**The ultimate dual-resource stamina overhaul for Minecraft 1.21+.**

Inspired by the high-stakes movement and combat of *Arena Breakout: Infinite*, this plugin replaces the arcade feel of Minecraft with a tactical, weight-based system where every breath and swing counts.

---

## 🏃‍♂️ Physical (Legs) vs. ⚔️ Combat (Arms)
The plugin tracks two independent pools. If you push your body to the limit, exhaustion sets in.

### 📊 Exhaustion & Cost Table
| Action | Cost | Category |
|---|---|---|
| **Sprinting** | 0.35 / tick | Leg Stamina |
| **Jumping** | 8.0 units | Leg Stamina |
| **Tactical Jump (Sneaking)** | 4.0 units | Leg Stamina (50% saved) |
| **Fall Damage** | 2.0 per HP | Leg Stamina |
| **Weapon Swing** | 10.0 units | Arm Stamina |
| **Bow Shot** | 15.0 units | Arm Stamina |
| **Mining Blocks** | 1.5 units | Arm Stamina |
| **Shield Impact** | 12.0 units | Arm Stamina |

---

## 🧩 Core Mechanics

### ⚠️ Exhaustion Penalties
* **Legs (0%):** Triggers **Slowness V**. You cannot sprint and move at a crawl until you recover to 25%.
* **Arms (0%):** Triggers **Weakness II** and **Mining Fatigue III** for 4 seconds. Your swings become heavy and ineffective.

### 🛡️ Shield Break
Taking hits while blocking consumes Arm stamina. If your Arm stamina hits **0% while blocking**, your shield is put on a **5-second cooldown**, leaving you vulnerable.

### 🍕 Survival & Hunger Cap
If your food level is **6 or lower (3 chunks)**, your stamina is hard-capped at **50.0**. You cannot reach full strength or peak performance while starving.

### ⚡ Adrenaline System
Consuming a **Golden Apple**, **Enchanted Golden Apple**, or a **Speed Potion** activates an Adrenaline Rush.
* **Duration:** 10 Seconds
* **Effect:** All stamina drain is completely ignored for the duration.

---

## 🔄 Regeneration & HUD
* **Regeneration:** Starts after 1 second (1000ms) of inactivity for that limb.
    * **Normal:** 0.4 / tick
    * **Sneaking:** 0.8 / tick (2x faster)
* **HUD Display:** Real-time updates in the Action Bar.
    * `L: [||||||||||] | A: [||||||||||]`
    * **Green/Cyan:** Healthy
    * **Red:** Hunger Capped (≤6 food)
    * **Dark Red:** Critical (<10% stamina)

---

## 🔧 Technical Information
* **Server:** Spigot / Paper 1.18 - 1.21+
* **Developer:** [Swartzz](https://github.com/Schwzz)
* **More:** [Modrinth](https://modrinth.com/plugin/dualstamina)
