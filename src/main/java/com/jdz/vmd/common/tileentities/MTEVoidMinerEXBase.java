package com.jdz.vmd.common.tileentities;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GTValues.VN;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

public abstract class MTEVoidMinerEXBase<T extends MTEVoidMinerEXBase<T>> extends MTEVoidMinerBase<T> {

    public MTEVoidMinerEXBase(int aID, String aName, String aNameRegional, int multiplier) {
        super(aID, aName, aNameRegional, multiplier);
    }

    public MTEVoidMinerEXBase(String aName, int tier) {
        super(aName, tier);
        casingTextureIndex = getControllerTextureIndex();
    }

    abstract public Materials getMaterials();

    abstract public ItemList getItemList();

    protected IStructureDefinition<T> buildBaseStructureDefinition(Class<T> clazz) {
        return StructureDefinition.<T>builder()
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
                    getItemList().getBlock(),
                    getItemList().get(0).getItemDamage())
            )
            .addElement('G', ofFrame(getMaterials()))
            .addElement(
                'H',
                buildHatchAdder(clazz).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                    .dot(1)
                    .casingIndex(getControllerTextureIndex())
                    .buildAndChain(
                        getItemList().getBlock(),
                        getItemList().get(0).getItemDamage()))
            .build();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Miner")
            .addInfo("Consumes " + numberFormat.format(GTValues.V[this.getMinTier()]) + "EU/t")
            .addInfo(
                "Can be supplied with " + EnumChatFormatting.AQUA
                    + "2 L/s"
                    + EnumChatFormatting.GRAY
                    + " of Noble gases to boost "
                    + EnumChatFormatting.GOLD
                    + "output")
            .addInfo(createGasString(EnumChatFormatting.LIGHT_PURPLE, "Neon", 4))
            .addInfo(createGasString(EnumChatFormatting.AQUA, "Krypton", 8))
            .addInfo(createGasString(EnumChatFormatting.DARK_AQUA, "Xenon", 16))
            .addInfo(createGasString(EnumChatFormatting.BLUE, "Oganesson", 64))
            .addInfo(
                "Will output " + 2 * this.TIER_MULTIPLIER
                    + " Ores per Second depending on the Dimension it is build in")
            .addInfo("Put the Ore into the input bus to set the Whitelist/Blacklist")
            .addInfo("Use a screwdriver to toggle Whitelist/Blacklist")
            .addInfo(
                "Blacklist or non Whitelist Ore will be " + EnumChatFormatting.DARK_RED
                    + "VOIDED"
                    + EnumChatFormatting.RESET
                    + ".")
            .beginStructureBlock(3, 7, 3, false)
            .addStructureInfo(
                EnumChatFormatting.GOLD + "5x" + EnumChatFormatting.GRAY + " " + GTOreDictUnificator.get(OrePrefixes.itemCasing, getMaterials(), 1).getDisplayName())
            .addStructureInfo(
                EnumChatFormatting.GOLD + "20x" + EnumChatFormatting.GRAY + " " + GTOreDictUnificator.get(OrePrefixes.frameGt, getMaterials(), 1).getDisplayName())
            .addEnergyHatch(VN[this.getMinTier()] + "+, Any base casing")
            .addMaintenanceHatch("Any base casing")
            .addInputBus("Mining Pipes or Ores, optional, any base casing")
            .addInputHatch("Optional noble gas, any base casing")
            .addOutputBus("Any base casing")
            .toolTipFinisher();
        return tt;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 1, 6, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece("main", stackSize, 1, 6, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece("main", 1, 6, 0)
            && checkHatches()
            && GTUtility.getTier(getMaxInputVoltage()) >= getMinTier()
            && mMaintenanceHatches.size() == 1;
    }

    @Override
    protected List<IHatchElement<? super MTEDrillerBase>> getAllowedHatches() {
        return ImmutableList.of(InputBus, OutputBus, Maintenance, Energy);
    }
}
