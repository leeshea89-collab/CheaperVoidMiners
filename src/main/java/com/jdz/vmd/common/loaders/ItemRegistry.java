package com.jdz.vmd.common.loaders;

import static com.jdz.vmd.api.enums.MetaTileEntityIDs.VoidMinerEX1Controller;

import com.jdz.vmd.common.tileentities.MTEUpgradeableNativeVoidMiner;
import net.minecraft.item.ItemStack;

/** Register one scalable LV-through-IV Void Miner controller. */
public class ItemRegistry {
    public static ItemStack VoidMinerEX1;

    public static void run() {
        System.out.println("[CheaperVoidMiners] Registering LV-IV native-base miner, ID=" + VoidMinerEX1Controller.ID);
        VoidMinerEX1 = new MTEUpgradeableNativeVoidMiner(
            VoidMinerEX1Controller.ID, "cvmUpgradeableMiner", "Upgradeable Void Miner (LV-IV)")
            .getStackForm(1L);
        System.out.println("[CheaperVoidMiners] LV-IV native-base miner registered");
    }
}
