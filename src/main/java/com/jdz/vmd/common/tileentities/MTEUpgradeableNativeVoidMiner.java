package com.jdz.vmd.common.tileentities;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.List;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV;
import gregtech.api.casing.Casings;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Affordable LV-through-IV Void Miner.
 *
 * Inherits native GTNH Void Miner ore selection, filtering, GUI, gas boosting,
 * and VMTweak's MTEVoidMinerBase mixins. Only the machine's structure and
 * voltage/output scale are changed.
 */
public class MTEUpgradeableNativeVoidMiner extends VMLUV {

    private static final String STRUCTURE_PIECE = "compact";

    /**
     * Keep the structure lazy: machine registration must not resolve casing
     * ItemStacks while GregTech is still populating its item registries.
     */
    private static final class CompactStructure {
        static final IStructureDefinition<VMLUV> DEFINITION = StructureDefinition.<VMLUV>builder()
            .addShape(
                STRUCTURE_PIECE,
                transpose(new String[][] {
                    { " F ", "FCF", " F " },
                    { " F ", "FCF", " F " },
                    { "H~H", "HHH", "HHH" }
                }))
            .addElement('C', Casings.SolidSteelMachineCasing.asElement())
            .addElement('F', ofFrame(Materials.Steel))
            .addElement(
                'H',
                buildHatchAdder(VMLUV.class)
                    .atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                    .casingIndex(Casings.SolidSteelMachineCasing.getTextureId())
                    .buildAndChain(Casings.SolidSteelMachineCasing.asElement()))
            .build();
    }

    public MTEUpgradeableNativeVoidMiner(int id, String name, String regionalName) {
        super(id, name, regionalName);
        casingTextureIndex = Casings.SolidSteelMachineCasing.getTextureId();
    }

    private MTEUpgradeableNativeVoidMiner(String name) {
        super(name, 1);
        // Native VMLUV clones default to Osmiridium; switch the texture to steel.
        casingTextureIndex = Casings.SolidSteelMachineCasing.getTextureId();
    }

    @Override
    protected int getControllerTextureIndex() {
        return Casings.SolidSteelMachineCasing.getTextureId();
    }

    @Override
    public IStructureDefinition<VMLUV> getStructureDefinition() {
        return CompactStructure.DEFINITION;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        return new MultiblockTooltipBuilder()
            .addMachineType("LV-IV Void Miner")
            .addInfo("Starts at LV; automatically scales with MV, HV, EV and IV energy hatches.")
            .addInfo("Base output per second: LV 8 / MV 12 / HV 16 / EV 24 / IV 32 ores.")
            .addInfo("Uses normal GTNH Void Miner ore selection, filters and noble-gas boosts.")
            .beginStructureBlock(3, 3, 3, false)
            .addController("Bottom front centre")
            .addCasing("8", "Steel Frame Box", false)
            .addCasing("2", "Solid Steel Machine Casing (centre shaft)", false)
            .addCasing("0-8", "Solid Steel Machine Casing (base; hatches replace casings)", false)
            .addEnergyHatch("1+", "Any base casing (LV-IV)", 1)
            .addMaintenanceHatch("1", "Any base casing", 1)
            .addOutputBus("1+", "Any base casing", 1)
            .addInputBus("0+", "Any base casing (optional ore filter)", 1)
            .addInputHatch("0+", "Any base casing (optional noble gas)", 1)
            .toolTipFinisher();
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE, stackSize, hintsOnly, 1, 2, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE, stackSize, 1, 2, 0, elementBudget, env, false, true);
    }

    @Override
    public void checkMachine(IGregTechTileEntity base, ItemStack stack, List<StructureError> errors) {
        if (!checkPiece(STRUCTURE_PIECE, 1, 2, 0, errors)) return;
        checkHatches(errors);
        if (GTUtility.getTier(getMaxInputVoltage()) < getMinTier()) {
            errors.add(StructureErrors.energyHatchTierTooLow(getMinTier()));
        }
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
        // Retain native ore sampling, dimension drops, filtering and VMTweak.
        final int[] outputRunsPerCycle = { 4, 6, 8, 12, 16 };
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
