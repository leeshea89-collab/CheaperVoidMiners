package com.jdz.vmd.common.tileentities;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.GregTechAPI;
import gregtech.api.casing.Casings;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.block.ModBlocks;
import net.minecraft.item.ItemStack;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

public class MTEVoidMiners {

    public static class VMLV extends MTEVoidMinerEXBase<VMLV> {

        public VMLV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 4);
        }

        public VMLV(String aName, int tier) {
            super(aName, tier);
            casingTextureIndex = GTUtility.getCasingTextureIndex(ItemList.Casing_SolidSteel.getBlock(), 0);
        }

        @Override
        protected int getMinTier() {
            return 1; // LV
        }

        @Override
        public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, net.minecraft.item.ItemStack aStack) {
            return checkPiece("main", 1, 6, 0)
                && checkHatches()
                && GTUtility.getTier(getMaxInputVoltage()) >= getMinTier()
                && mMaintenanceHatches.size() == 1;
        }

        @Override
        public void construct(net.minecraft.item.ItemStack stackSize, boolean hintsOnly) {
            buildPiece("main", stackSize, hintsOnly, 1, 6, 0);
        }

        @Override
        public IStructureDefinition<VMLV> getStructureDefinition() {
            return StructureDefinition.<VMLV>builder()
                .addShape(
                    "main",
                    transpose(
                        new String[][]{
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {"H~H", "HHH", "HHH"}}))
                .addElement(
                    'F',
                    ofBlock(
                        ItemList.Casing_LV.getBlock(),
                        ItemList.Casing_LV.get(0)
                            .getItemDamage()))
                .addElement('G', ofFrame(Materials.Steel))
                .addElement(
                    'H',
                    buildHatchAdder(VMLV.class).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                        .dot(1)
                        .casingIndex(8)
                        .buildAndChain(
                            ItemList.Casing_LV.getBlock(),
                            ItemList.Casing_LV.get(0)
                                .getItemDamage()))
                .build();
        }

        @Override
        public int survivalConstruct(net.minecraft.item.ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
            if (mMachine) return -1;
            return survivalBuildPiece("main", stackSize, 1, 6, 0, elementBudget, env, false, true);
        }

        @Override
        protected int getControllerTextureIndex() {
            return 16;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
            return new VMLV(this.mName, this.TIER_MULTIPLIER);
        }

    }

    public static class VMHV extends MTEVoidMinerEXBase<VMHV> {

        public VMHV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 8);
            casingTextureIndex = GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 1);
        }

        public VMHV(String aName, int tier) {
            super(aName, tier);
            casingTextureIndex = GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 1);
        }

        @Override
        protected int getMinTier() {
            return 3;
        }

        @Override
        public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
            return checkPiece("main", 1, 6, 0)
                && checkHatches()
                && GTUtility.getTier(getMaxInputVoltage()) >= getMinTier()
                && mMaintenanceHatches.size() == 1;
        }

        @Override
        public void construct(ItemStack stackSize, boolean hintsOnly) {
            buildPiece("main", stackSize, hintsOnly, 1, 6, 0);
        }

        @Override
        public IStructureDefinition<VMHV> getStructureDefinition() {
            return StructureDefinition.<VMHV>builder()
                .addShape(
                    "main",
                    transpose(
                        new String[][]{
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {"H~H", "HHH", "HHH"}}))
                .addElement(
                    'F',
                    ofBlock(
                        ItemList.Casing_HV.getBlock(),
                        ItemList.Casing_HV.get(0)
                            .getItemDamage()))
                .addElement('G', ofFrame(Materials.StainlessSteel))
                .addElement(
                    'H',
                    buildHatchAdder(VMHV.class).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                        .dot(1)
                        .casingIndex(8)
                        .buildAndChain(
                            ItemList.Casing_HV.getBlock(),
                            ItemList.Casing_HV.get(0)
                                .getItemDamage()))
                .build();
        }

        @Override
        public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
            if (mMachine) return -1;
            return survivalBuildPiece("main", stackSize, 1, 6, 0, elementBudget, env, false, true);
        }

        @Override
        protected int getControllerTextureIndex() {
            return GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 1);
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
            casingTextureIndex = GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 0);
        }

        @Override
        protected int getMinTier() {
            return 5;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
            return new VMIV(this.mName, this.TIER_MULTIPLIER);
        }

        @Override
        public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
            return checkPiece("main", 1, 6, 0)
                && checkHatches()
                && GTUtility.getTier(getMaxInputVoltage()) >= getMinTier()
                && mMaintenanceHatches.size() == 1;
        }

        @Override
        public void construct(ItemStack stackSize, boolean hintsOnly) {
            buildPiece("main", stackSize, hintsOnly, 1, 6, 0);
        }

        @Override
        public IStructureDefinition<VMIV> getStructureDefinition() {
            return StructureDefinition.<VMIV>builder()
                .addShape(
                    "main",
                    transpose(
                        new String[][]{
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {"   ", " G ", "   "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {" G ", "GFG", " G "},
                            {"H~H", "HHH", "HHH"}}))
                .addElement(
                    'F',
                    ofBlock(
                        ItemList.Casing_IV.getBlock(),
                        ItemList.Casing_IV.get(0)
                            .getItemDamage()))
                .addElement('G', ofFrame(Materials.TungstenSteel))
                .addElement(
                    'H',
                    buildHatchAdder(VMIV.class).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                        .dot(1)
                        .casingIndex(8)
                        .buildAndChain(
                            ItemList.Casing_IV.getBlock(),
                            ItemList.Casing_IV.get(0)
                                .getItemDamage()))
                .build();
        }

        @Override
        public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
            if (mMachine) return -1;
            return survivalBuildPiece("main", stackSize, 1, 6, 0, elementBudget, env, false, true);
        }

        @Override
        protected int getControllerTextureIndex() {
            return GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 0);
        }
    }
}
