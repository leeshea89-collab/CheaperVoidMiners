package com.jdz.vmd.common.tileentities;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

/**
 * LV-to-IV scalable void miner, based directly on GTNH's known-working
 * native VMLUV implementation. Keep its original 7x9x7 geometry,
 * replacing Osmiridium structural components with LV-era steel variants.
 *
 * VMTweak patches MTEVoidMinerBase, which is inherited here.
 */
public class MTEUpgradeableNativeVoidMiner extends bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    // Identical geometry, hatch requirements and controller position to GTNH's
    // original VMLUV. Only the casing/frame materials have been substituted.
    // Keeping this structure definition in a nested holder defers its creation
    // until structure/NEI inspection, avoiding early registry initialization.
    private static final class SteelStructure {
        static final IStructureDefinition<VMLUV> STRUCTURE_DEFINITION = StructureDefinition.<VMLUV>builder()
            // spotless:off
            .addShape(
                STRUCTURE_PIECE_MAIN,
                transpose(
                    new String[][] {
                        { "       ", "       ", "       ", "   B   ", "       ", "       ", "       " },
                        { "       ", "       ", "       ", "   B   ", "       ", "       ", "       " },
                        { "       ", "       ", "       ", "   B   ", "       ", "       ", "       " },
                        { "       ", "       ", "   B   ", "  BCB  ", "   B   ", "       ", "       " },
                        { "       ", "       ", "   B   ", "  BCB  ", "   B   ", "       ", "       " },
                        { "       ", "       ", "   B   ", "  BCB  ", "   B   ", "       ", "       " },
                        { "       ", " B   B ", "  DAD  ", "  ACA  ", "  DAD  ", " B   B ", "       " },
                        { "  D D  ", " BA~AB ", " A   A ", " B C B ", " A   A ", " BABAB ", "       " },
                        { "  E E  ", " BBBBB ", "EB   BE", " B C B ", "EB   BE", " BBBBB ", "  E E  " } }))
            // spotless:on
            .addElement('A', Casings.SolidSteelMachineCasing.asElement())
            .addElement('B', ofFrame(Materials.Steel))
            .addElement('C', Casings.SteelGearBoxCasing.asElement())
            .addElement('D', Casings.SteelPipeCasing.asElement())
            .addElement(
                'E',
                buildHatchAdder(VMLUV.class).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                    .hint(1)
                    .casingIndex(Casings.SolidSteelMachineCasing.getTextureId())
                    .buildAndChain(Casings.SolidSteelMachineCasing.asElement()))
            .build();
    }

    @Override
    public IStructureDefinition<bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV> getStructureDefinition() {
        return SteelStructure.STRUCTURE_DEFINITION;
    }

    @Override
    protected int getControllerTextureIndex() {
        return Casings.SolidSteelMachineCasing.getTextureId();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        return new MultiblockTooltipBuilder()
            .addMachineType("LV-IV Void Miner")
            .addInfo("Native GTNH Void Miner shape, but made from steel.")
            .addInfo("Output scales with input voltage: LV 8, MV 12, HV 16, EV 24, IV 32 ores/s.")
            .addInfo("GTNH and VMTweak ore selection, ore filtering and noble gas boosts are preserved.")
            .beginStructureBlock(7, 9, 7, false)
            .addController("Front centre, second layer")
            .addCasing("42", "Steel Frame Box", false)
            .addCasing("12-17", "Solid Steel Machine Casing", false)
            .addCasing("6", "Steel Gear Box Casing", false)
            .addCasing("6", "Steel Pipe Casing", false)
            .addEnergyHatch("1+", "Any leg casing (LV-IV)", 1)
            .addMaintenanceHatch("1", "Any leg casing", 1)
            .addInputBus("0+", "Any leg casing (optional ore filter)", 1)
            .addInputHatch("0+", "Any leg casing (optional noble gas)", 1)
            .addOutputBus("1+", "Any leg casing", 1)
            .toolTipFinisher();
    }


    public MTEUpgradeableNativeVoidMiner(int id, String name, String regionalName) {
        super(id, name, regionalName);
        casingTextureIndex = Casings.SolidSteelMachineCasing.getTextureId();
    }

    private MTEUpgradeableNativeVoidMiner(String name) {
        super(name, 1);
        // GTNH VMLUV clone constructor normally sets the Osmiridium texture.
        casingTextureIndex = Casings.SolidSteelMachineCasing.getTextureId();
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
