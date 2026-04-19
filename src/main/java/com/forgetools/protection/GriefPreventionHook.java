package com.forgetools.protection;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GriefPreventionHook {

    public boolean canBuild(Player player, Location location) {
        try {
            String reason = GriefPrevention.instance.allowBuild(player, location, Material.AIR);
            return reason == null;
        } catch (Exception e) {
            return true;
        }
    }
}
