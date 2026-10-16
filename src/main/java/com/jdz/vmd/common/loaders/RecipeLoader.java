package com.jdz.vmd.common.loaders;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import net.minecraft.item.ItemStack;

/**
 * Cheap LV-era controller recipe. Uses GTNH's buffered crafting registration
 * rather than the raw vanilla GameRegistry path.
 */
public final class RecipeLoader {

    private RecipeLoader() {}

    public static void run() {
        ItemStack lvMiner = ItemList.Machine_LV_Miner.get(1);
        ItemStack steelPlate = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 1L);
        if (ItemRegistry.VoidMinerEX1 == null || lvMiner == null || steelPlate == null) {
            System.err.println("[CheaperVoidMiners] Cheap LV crafting recipe could not be registered: missing ingredient.");
            return;
        }

        boolean registered = GTModHandler.addShapelessCraftingRecipe(
            ItemRegistry.VoidMinerEX1.copy(),
            new Object[] {
                lvMiner,
                steelPlate.copy(),
                steelPlate.copy(),
                steelPlate.copy(),
                steelPlate.copy()
            });

        System.out.println("[CheaperVoidMiners] LV Miner + 4 Steel Plates recipe registration: " + registered);
    }
}
