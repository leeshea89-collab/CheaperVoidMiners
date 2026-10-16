package com.jdz.vmd.common.loaders;

import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerEX1Controller;

import gregtech.api.metatileentity.implementations.MTEBasicHull;
import net.minecraft.item.ItemStack;

/** Temporary diagnostic: registers an ordinary GT machine, without Void Miner inheritance. */
public class ItemRegistry {
    public static ItemStack VoidMinerEX1;

    public static void run() {
        System.out.println("[CheaperVoidMiners/PROBE] Before registering a plain GT LV hull, ID=" + VoidMinerEX1Controller.ID);
        VoidMinerEX1 = new MTEBasicHull(VoidMinerEX1Controller.ID, "voidMinerRegistrationProbe",
            "Void Miner Registration Probe - NOT A MINER", 1).getStackForm(1L);
        System.out.println("[CheaperVoidMiners/PROBE] Plain LV hull registration completed; stack=" + VoidMinerEX1);
    }
}
