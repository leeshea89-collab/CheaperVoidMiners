package com.jdz.vmd.common.loaders;

import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerEVController;
import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerEX1Controller;
import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerHVController;
import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerIVController;
import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerLVController;
import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerMVController;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.jdz.vmd.common.tileentities.MTETieredVoidMiner;
import com.jdz.vmd.common.tileentities.MTETieredVoidMiner.Tier;
import com.jdz.vmd.Config;
import com.jdz.vmd.common.tileentities.MTEUpgradeableNativeVoidMiner;
import com.jdz.vmd.common.tileentities.MTEVoidFluidDrill;

import gregtech.api.GregTechAPI;

/** Five new tiers + the original controller, preserved for existing worlds. */
public final class ItemRegistry {

    public static ItemStack VoidMinerEX1; // Legacy ID 31763, still upgradeable from LV to IV.
    private static final Map<Tier, ItemStack> TIERED_CONTROLLERS = new EnumMap<>(Tier.class);
    private static final Map<Tier, ItemStack> FLUID_DRILLS = new EnumMap<>(Tier.class);

    private ItemRegistry() {}

    public static ItemStack get(Tier tier) {
        return TIERED_CONTROLLERS.get(tier);
    }

    public static ItemStack fluidDrill(Tier tier) {
        return FLUID_DRILLS.get(tier);
    }

    public static void run() {
        System.out.println("[CheaperVoidMiners] Preserving legacy native-shape miner at " + VoidMinerEX1Controller.ID);
        checkIdFree(VoidMinerEX1Controller.ID);
        VoidMinerEX1 = new MTEUpgradeableNativeVoidMiner(
            VoidMinerEX1Controller.ID, "cvmUpgradeableMiner", "Upgradeable Void Miner (LV-IV) [Legacy]")
            .getStackForm(1L);

        register(Tier.LV, VoidMinerLVController.ID, "cvmTieredMinerLV", "Void Miner (LV)");
        register(Tier.MV, VoidMinerMVController.ID, "cvmTieredMinerMV", "Void Miner (MV)");
        register(Tier.HV, VoidMinerHVController.ID, "cvmTieredMinerHV", "Void Miner (HV)");
        register(Tier.EV, VoidMinerEVController.ID, "cvmTieredMinerEV", "Void Miner (EV)");
        register(Tier.IV, VoidMinerIVController.ID, "cvmTieredMinerIV", "Void Miner (IV)");
        System.out.println("[CheaperVoidMiners] Five fixed-tier Void Miner controllers registered.");
        registerFluidDrills();
    }

    /** Void Fluid Drills LV-IV. Never crashes the game: a taken id is logged and that drill skipped. */
    private static void registerFluidDrills() {
        if (Config.fluidDrillBaseId <= 0) {
            System.out.println("[CheaperVoidMiners] Void Fluid Drills are turned off in the config.");
            return;
        }
        for (Tier tier : Tier.values()) {
            int id = Config.fluidDrillBaseId + tier.ordinal();
            if (id >= GregTechAPI.METATILEENTITIES.length || GregTechAPI.METATILEENTITIES[id] != null) {
                System.out.println(
                    "[CheaperVoidMiners] Machine id " + id + " is taken; the " + tier
                        + " Void Fluid Drill is skipped. Change fluid_drill.baseMachineId in the config.");
                continue;
            }
            ItemStack stack = new MTEVoidFluidDrill(
                id, "cvmVoidFluidDrill" + tier.name(), "Void Fluid Drill (" + tier.name() + ")", tier)
                .getStackForm(1L);
            FLUID_DRILLS.put(tier, stack);
            System.out.println("[CheaperVoidMiners] Registered " + tier + " Void Fluid Drill as MTE " + id);
        }
    }

    private static void checkIdFree(int id) {
        if (id < 0 || id >= GregTechAPI.METATILEENTITIES.length) {
            throw new IllegalStateException("[CheaperVoidMiners] Invalid GregTech machine ID " + id);
        }
        if (GregTechAPI.METATILEENTITIES[id] != null) {
            throw new IllegalStateException(
                "[CheaperVoidMiners] GregTech machine ID " + id + " is occupied by "
                    + GregTechAPI.METATILEENTITIES[id].getMetaName());
        }
    }

    private static void register(Tier tier, int id, String name, String localName) {
        checkIdFree(id);
        ItemStack stack = new MTETieredVoidMiner(id, name, localName, tier).getStackForm(1L);
        TIERED_CONTROLLERS.put(tier, stack);
        System.out.println("[CheaperVoidMiners] Registered " + tier.name() + " controller as MTE " + id);
    }
}
