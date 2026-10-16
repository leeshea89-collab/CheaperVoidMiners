package com.jdz.vmd.common.loaders;

import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerEX1Controller;

import net.minecraft.item.ItemStack;

/** Diagnostic only: register GregTech's own Void Miner subclass at our working test ID. */
public class ItemRegistry {
    public static ItemStack VoidMinerEX1;

    public static void run() {
        System.out.println("[CheaperVoidMiners/PROBE] Registering native GregTech Void Miner at ID " + VoidMinerEX1Controller.ID);
        VoidMinerEX1 = new bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV(
            VoidMinerEX1Controller.ID, "cvmNativeVoidProbe", "Native Void Miner Probe - NOT LV")
            .getStackForm(1L);
        System.out.println("[CheaperVoidMiners/PROBE] Native GregTech Void Miner registered successfully");
    }
}
