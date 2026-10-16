package com.jdz.vmd.common.loaders;

import com.jdz.vmd.common.tileentities.MTETieredVoidMiner.Tier;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import net.minecraft.item.ItemStack;

/**
 * Progressive 1x1 crafting-grid recipes. A previous-tier Void Miner is
 * consumed on each upgrade, so every tier has a distinct production cost.
 * Recipes are registered after GTNH items are available.
 */
public final class RecipeLoader {

    private RecipeLoader() {}

    private static ItemStack plate(Materials material) {
        return GTOreDictUnificator.get(OrePrefixes.plate, material, 1L);
    }

    private static void register(Tier tier, Object... ingredients) {
        ItemStack result = ItemRegistry.get(tier);
        if (result == null) {
            throw new IllegalStateException("Missing " + tier + " Void Miner controller registration");
        }
        if (ingredients.length > 9) {
            throw new IllegalArgumentException("Void Miner " + tier + " recipe has more than nine ingredients");
        }
        for (Object ingredient : ingredients) {
            if (ingredient == null) {
                throw new IllegalStateException("Missing GregTech ingredient for " + tier + " Void Miner");
            }
        }

        boolean added = GTModHandler.addShapelessCraftingRecipe(result.copy(), ingredients);
        if (!added) {
            throw new IllegalStateException("GregTech refused " + tier + " Void Miner crafting recipe");
        }
        System.out.println("[CheaperVoidMiners] Registered " + tier + " Void Miner crafting recipe.");
    }

    public static void run() {
        // LV: starting point. The LV Miner supplies the actual mining hardware.
        register(Tier.LV,
            ItemList.Machine_LV_Miner.get(1L),
            plate(Materials.Steel),
            plate(Materials.Steel),
            ItemList.Electric_Motor_LV.get(1L),
            ItemList.Electric_Piston_LV.get(1L),
            "circuitLV");

        // MV: consume the previous miner, then upgrade motors, circuits, and casing stock.
        register(Tier.MV,
            ItemRegistry.get(Tier.LV),
            ItemList.Machine_MV_Miner.get(1L),
            plate(Materials.Aluminium),
            plate(Materials.Aluminium),
            ItemList.Electric_Motor_MV.get(1L),
            ItemList.Electric_Piston_MV.get(1L),
            "circuitMV");

        // HV adds a dedicated fluid pump for mining-gas infrastructure.
        register(Tier.HV,
            ItemRegistry.get(Tier.MV),
            ItemList.Machine_HV_Miner.get(1L),
            plate(Materials.StainlessSteel),
            plate(Materials.StainlessSteel),
            ItemList.Electric_Motor_HV.get(1L),
            ItemList.Electric_Piston_HV.get(1L),
            ItemList.Electric_Pump_HV.get(1L),
            "circuitHV");

        // EV and IV require a machine hull and a robotic arm as well.
        // Standard GregTech has only LV, MV and HV dedicated Miner machines.
        register(Tier.EV,
            ItemRegistry.get(Tier.HV),
            ItemList.Hull_EV.get(1L),
            plate(Materials.Titanium),
            plate(Materials.Titanium),
            ItemList.Electric_Motor_EV.get(1L),
            ItemList.Electric_Piston_EV.get(1L),
            ItemList.Electric_Pump_EV.get(1L),
            ItemList.Robot_Arm_EV.get(1L),
            "circuitEV");

        register(Tier.IV,
            ItemRegistry.get(Tier.EV),
            ItemList.Hull_IV.get(1L),
            plate(Materials.TungstenSteel),
            plate(Materials.TungstenSteel),
            ItemList.Electric_Motor_IV.get(1L),
            ItemList.Electric_Piston_IV.get(1L),
            ItemList.Electric_Pump_IV.get(1L),
            ItemList.Robot_Arm_IV.get(1L),
            "circuitIV");

        // Deliberately no recipe for the legacy controller; existing blocks
        // retain their old ID while new survival progression uses five tiers.
    }
}
