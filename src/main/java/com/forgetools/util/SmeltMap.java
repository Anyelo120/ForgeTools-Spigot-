package com.forgetools.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public final class SmeltMap {

    private SmeltMap() {}

    public static final Map<Material, Material> RECIPES = new HashMap<>();

    static {
        RECIPES.put(Material.IRON_ORE, Material.IRON_INGOT);
        RECIPES.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        RECIPES.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        RECIPES.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        RECIPES.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        RECIPES.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        RECIPES.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        RECIPES.put(Material.STONE, Material.SMOOTH_STONE);
        RECIPES.put(Material.COBBLESTONE, Material.STONE);
        RECIPES.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
        RECIPES.put(Material.SAND, Material.GLASS);
        RECIPES.put(Material.GRAVEL, Material.FLINT);
        RECIPES.put(Material.CLAY, Material.TERRACOTTA);
        RECIPES.put(Material.NETHERRACK, Material.NETHER_BRICK);
        RECIPES.put(Material.RAW_IRON, Material.IRON_INGOT);
        RECIPES.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        RECIPES.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        RECIPES.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        RECIPES.put(Material.CACTUS, Material.GREEN_DYE);
        RECIPES.put(Material.SEA_PICKLE, Material.LIME_DYE);
        RECIPES.put(Material.WET_SPONGE, Material.SPONGE);
        RECIPES.put(Material.CHORUS_FRUIT, Material.POPPED_CHORUS_FRUIT);
    }

    public static Material getSmeltResult(Material input) {
        return RECIPES.get(input);
    }
}
