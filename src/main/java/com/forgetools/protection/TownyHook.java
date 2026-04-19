package com.forgetools.protection;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyHook {

    public boolean canBuild(Player player, Location location) {
        try {
            TownyAPI api = TownyAPI.getInstance();
            if (api == null) return true;

            TownBlock townBlock = api.getTownBlock(location);
            if (townBlock == null) return true;

            Resident resident = api.getResident(player.getUniqueId());
            if (resident == null) return false;

            if (!townBlock.hasTown()) return true;

            try {
                if (townBlock.getTown().hasResident(resident)) return true;
            } catch (NotRegisteredException ignored) {}

            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
