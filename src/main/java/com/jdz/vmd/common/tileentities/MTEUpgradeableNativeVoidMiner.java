package com.jdz.vmd.common.tileentities;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

/**
 * LV-to-IV scalable void miner, based directly on GTNH's known-working
 * native VMLUV implementation. The expensive native multiblock is retained
 * only in this diagnostic build, to isolate the startup bug from structure changes.
 *
 * VMTweak patches MTEVoidMinerBase, which is inherited here.
 */
public class MTEUpgradeableNativeVoidMiner extends bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV {

    public MTEUpgradeableNativeVoidMiner(int id, String name, String regionalName) {
        super(id, name, regionalName);
    }

    private MTEUpgradeableNativeVoidMiner(String name) {
        super(name, 1);
    }

    @Override
    protected int getMinTier() {
        return VoltageIndex.LV;
    }

    private int availablePowerTier() {
        return Math.min(VoltageIndex.IV,
            Math.max(VoltageIndex.LV, GTUtility.getTier(getMaxInputVoltage())));
    }

    @Override
    protected void setElectricityStats() {
        super.setElectricityStats();
        mEUt = -Math.abs(Math.toIntExact(GTValues.VP[availablePowerTier()]));
    }

    @Override
    protected void outputAfterRecipe() {
        // GTNH's native tier-multiplier 1 emits two ore items per second.
        // Repeat its existing output routine to preserve native ore sampling,
        // dimension handling, filters and VMTweak integration.
        final int[] outputRunsPerCycle = {4, 6, 8, 12, 16};
        int runs = outputRunsPerCycle[availablePowerTier() - VoltageIndex.LV];
        for (int i = 0; i < runs; i++) {
            super.outputAfterRecipe();
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity tile) {
        return new MTEUpgradeableNativeVoidMiner(this.mName);
    }
}
