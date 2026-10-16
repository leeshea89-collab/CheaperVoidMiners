package com.jdz.vmd.common.tileentities;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

public class MTEVoidMiners {

    public static class VMLV extends MTEVoidMinerEXBase<VMLV> {

        public VMLV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 4);
        }

        public VMLV(String aName, int tier) {
            super(aName, tier);
        }

        @Override
        public ItemList getItemList() {
            return ItemList.Casing_SolidSteel;
        }

        @Override
        public Materials getMaterials() {
            return Materials.Steel;
        }

        @Override
        protected int getMinTier() {
            return VoltageIndex.LV; // LV
        }

        @Override
        protected int getControllerTextureIndex() {
            return GTUtility.getCasingTextureIndex(getItemList().getBlock(), 0);
        }

        @Override
        public IStructureDefinition<VMLV> getStructureDefinition() {
            return buildBaseStructureDefinition(VMLV.class);
        }

        // Each 10-tick mining cycle generates eight ores per second at LV.
        // Scaling is achieved by running the original void-miner output algorithm
        // additional times; this preserves VMTweak's dimension/drop-map integration.
        private int halfCycleRemainder = 0;

        private int currentPowerTier() {
            return Math.min(VoltageIndex.IV,
                Math.max(VoltageIndex.LV, GTUtility.getTier(getMaxInputVoltage())));
        }

        @Override
        protected void setElectricityStats() {
            super.setElectricityStats();
            // Charge the actual available energy tier instead of always charging LV.
            mEUt = -Math.abs(Math.toIntExact(gregtech.api.enums.GTValues.VP[currentPowerTier()]));
        }

        @Override
        protected void outputAfterRecipe() {
            // Number of half-runs of the base 8-ore/s machine:
            // LV=2, MV=3, HV=4, EV=6, IV=8.
            // Alternating the extra half-run at MV gives an average of 12/s.
            final int[] halfRuns = {2, 3, 4, 6, 8};
            final int desired = halfRuns[currentPowerTier() - VoltageIndex.LV];
            int repeats = desired / 2;
            halfCycleRemainder += desired % 2;
            if (halfCycleRemainder >= 2) {
                repeats++;
                halfCycleRemainder -= 2;
            }
            for (int i = 0; i < repeats; i++) {
                super.outputAfterRecipe();
            }
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
            return new VMLV(this.mName, this.TIER_MULTIPLIER);
        }

    }

    public static class VMHV extends MTEVoidMinerEXBase<VMHV> {

        public VMHV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 8);
        }

        public VMHV(String aName, int tier) {
            super(aName, tier);
        }

        @Override
        public ItemList getItemList() {
            return ItemList.Casing_CleanStainlessSteel;
        }

        @Override
        public Materials getMaterials() {
            return Materials.StainlessSteel;
        }

        @Override
        protected int getMinTier() {
            return VoltageIndex.HV;
        }

        @Override
        public IStructureDefinition<VMHV> getStructureDefinition() {
            return buildBaseStructureDefinition(VMHV.class);
        }

        @Override
        protected int getControllerTextureIndex() {
            return GTUtility.getCasingTextureIndex(getItemList().getBlock(), 1);
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
            return new VMHV(this.mName, this.TIER_MULTIPLIER);
        }

    }

    public static class VMIV extends MTEVoidMinerEXBase<VMIV> {

        public VMIV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 16);
        }

        public VMIV(String aName, int tier) {
            super(aName, tier);
        }

        @Override
        public ItemList getItemList() {
            return ItemList.Casing_RobustTungstenSteel;
        }

        @Override
        public Materials getMaterials() {
            return Materials.TungstenSteel;
        }

        @Override
        protected int getMinTier() {
            return VoltageIndex.IV;
        }

        @Override
        public IStructureDefinition<VMIV> getStructureDefinition() {
            return buildBaseStructureDefinition(VMIV.class);
        }

        @Override
        protected int getControllerTextureIndex() {
            return GTUtility.getCasingTextureIndex(getItemList().getBlock(), 0);
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
            return new VMIV(this.mName, this.TIER_MULTIPLIER);
        }
    }
}
