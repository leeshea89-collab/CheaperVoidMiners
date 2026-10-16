package com.jdz.vmd.common.loaders;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import net.minecraft.item.ItemStack;

/** Affordable shapeless LV recipe; no rare late-game components needed. */
public final class RecipeLoader {
    private RecipeLoader() {}

    public static void run() {
        ItemStack lvMiner = ItemList.Machine_LV_Miner.get(1);
        ItemStack plate = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 1L);
        if (ItemRegistry.VoidMinerEX1 == null || lvMiner == null || plate == null) {
            System.err.println("[CheaperVoidMiners] LV Void Miner recipe skipped: required item is missing.");
            return;
        }
        GameRegistry.addShapelessRecipe(
            ItemRegistry.VoidMinerEX1.copy(),
            lvMiner,
            plate.copy(), plate.copy(), plate.copy(), plate.copy());
        System.out.println("[CheaperVoidMiners] Crafting recipe registered: 1 LV Miner + 4 Steel Plates.");
    }
}
