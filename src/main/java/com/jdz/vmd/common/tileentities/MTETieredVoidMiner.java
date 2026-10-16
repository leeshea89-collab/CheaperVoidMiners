package com.jdz.vmd.common.tileentities;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.casing.Casings;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Fixed LV, MV, HV, EV, and IV versions of the native-base Void Miner.
 * Uses the original GTNH 7x9x7 geometry to preserve NEI structure previews,
 * construction offsets, hatch validation, and VMTweak ore/planet handling.
 */
public class MTETieredVoidMiner extends VMLUV {

    public enum Tier {
        LV(VoltageIndex.LV, 4, "Steel", "Solid Steel Machine Casing",
            "Steel Gear Box Casing", "Steel Pipe Casing"),
        MV(VoltageIndex.MV, 6, "Aluminium", "MV Machine Casing",
            "Frost Proof Machine Casing", "Steel Pipe Casing"),
        HV(VoltageIndex.HV, 8, "Stainless Steel", "HV Machine Casing",
            "Clean Stainless Steel Machine Casing", "Steel Gear Box Casing"),
        EV(VoltageIndex.EV, 12, "Titanium", "EV Machine Casing",
            "Stable Titanium Machine Casing", "Titanium Pipe Casing"),
        IV(VoltageIndex.IV, 16, "Tungstensteel", "IV Machine Casing",
            "Robust Tungstensteel Machine Casing", "Tungstensteel Pipe Casing");

        final int voltage;
        final int outputRuns;
        final String frameLabel;
        final String primaryLabel;
        final String secondaryLabel;
        final String pipeLabel;

        Tier(int voltage, int outputRuns, String frameLabel, String primaryLabel,
            String secondaryLabel, String pipeLabel) {
            this.voltage = voltage;
            this.outputRuns = outputRuns;
            this.frameLabel = frameLabel;
            this.primaryLabel = primaryLabel;
            this.secondaryLabel = secondaryLabel;
            this.pipeLabel = pipeLabel;
        }

        public Materials frameMaterial() {
            return switch (this) {
                case LV -> Materials.Steel;
                case MV -> Materials.Aluminium;
                case HV -> Materials.StainlessSteel;
                case EV -> Materials.Titanium;
                case IV -> Materials.TungstenSteel;
            };
        }

        public Casings primary() {
            return switch (this) {
                case LV -> Casings.SolidSteelMachineCasing;
                case MV -> Casings.MVMachineCasing;
                case HV -> Casings.HVMachineCasing;
                case EV -> Casings.EVMachineCasing;
                case IV -> Casings.IVMachineCasing;
            };
        }

        public Casings secondary() {
            return switch (this) {
                case LV -> Casings.SteelGearBoxCasing;
                case MV -> Casings.FrostProofMachineCasing;
                case HV -> Casings.CleanStainlessSteelMachineCasing;
                case EV -> Casings.StableTitaniumMachineCasing;
                case IV -> Casings.RobustTungstenSteelMachineCasing;
            };
        }

        public Casings pipe() {
            return switch (this) {
                case LV, MV -> Casings.SteelPipeCasing;
                case HV -> Casings.SteelGearBoxCasing;
                case EV -> Casings.TitaniumPipeCasing;
                case IV -> Casings.TungstensteelPipeCasing;
            };
        }

        public int oresPerSecond() {
            return outputRuns * 2;
        }
    }

    private static final String STRUCTURE_PIECE = "main";

    // Register controllers early, but defer structure casing lookup until NEI
    // or structure formation requests it. Keep the native shape unchanged.
    private static final class Structures {
        static final IStructureDefinition<VMLUV>[] DEFINITIONS = createAll();
    }

    @SuppressWarnings("unchecked")
    private static IStructureDefinition<VMLUV>[] createAll() {
        IStructureDefinition<VMLUV>[] result =
            (IStructureDefinition<VMLUV>[]) new IStructureDefinition<?>[Tier.values().length];
        for (Tier tier : Tier.values()) {
            result[tier.ordinal()] = build(tier);
        }
        return result;
    }

    private static IStructureDefinition<VMLUV> build(Tier tier) {
        Casings primary = tier.primary();
        return StructureDefinition.<VMLUV>builder()
            .addShape(
                STRUCTURE_PIECE,
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
            .addElement('A', primary.asElement())
            .addElement('B', ofFrame(tier.frameMaterial()))
            .addElement('C', tier.secondary().asElement())
            .addElement('D', tier.pipe().asElement())
            .addElement(
                'E',
                buildHatchAdder(VMLUV.class).atLeast(InputHatch, OutputBus, InputBus, Maintenance, Energy)
                    .hint(1)
                    .casingIndex(primary.getTextureId())
                    .buildAndChain(primary.asElement()))
            .build();
    }

    private Tier selectedTier;

    public MTETieredVoidMiner(int id, String name, String localName, Tier tier) {
        super(id, name, localName);
        selectedTier = tier;
        casingTextureIndex = tier.primary().getTextureId();
    }

    private MTETieredVoidMiner(String name, Tier tier) {
        super(name, 1);
        selectedTier = tier;
        casingTextureIndex = tier.primary().getTextureId();
    }

    private Tier tier() {
        // VMLUV calls a virtual texture getter from its constructor.
        return selectedTier == null ? Tier.LV : selectedTier;
    }

    @Override
    protected int getMinTier() {
        return tier().voltage;
    }

    @Override
    protected int getControllerTextureIndex() {
        return tier().primary().getTextureId();
    }

    @Override
    public IStructureDefinition<VMLUV> getStructureDefinition() {
        return Structures.DEFINITIONS[tier().ordinal()];
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        Tier t = tier();
        return new MultiblockTooltipBuilder()
            .addMachineType(t.name() + " Void Miner")
            .addInfo("A dedicated " + t.name() + "-tier Void Miner; upgrade by crafting the next controller.")
            .addInfo("Produces " + t.oresPerSecond() + " ores/s at normal operation.")
            .addInfo("Requires " + t.name() + " or better energy; faster hatches do not upgrade output.")
            .addInfo("Original GTNH Void Miner shape with tier-specific components.")
            .addInfo("Planet/ore selection, input filters and noble gases use native GTNH/VMTweak behavior.")
            .beginStructureBlock(7, 9, 7, false)
            .addController("Front centre, second layer")
            .addCasing("42", t.frameLabel + " Frame Boxes", false)
            .addCasing("12-17", t.primaryLabel, false)
            .addCasing("6", t.secondaryLabel, false)
            .addCasing("6", t.pipeLabel, false)
            .addEnergyHatch("1+", "Leg casing (" + t.name() + "+)", 1)
            .addMaintenanceHatch("1", "Leg casing", 1)
            .addOutputBus("1+", "Leg casing", 1)
            .addInputBus("0+", "Leg casing (optional filter)", 1)
            .addInputHatch("0+", "Leg casing (optional noble gas)", 1)
            .toolTipFinisher();
    }

    @Override
    protected void setElectricityStats() {
        super.setElectricityStats();
        mEUt = -Math.abs(Math.toIntExact(GTValues.VP[tier().voltage]));
    }

    @Override
    protected void outputAfterRecipe() {
        // GTNH/VMTweak native generation is used for each output sample.
        for (int i = 0; i < tier().outputRuns; i++) {
            super.outputAfterRecipe();
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity base) {
        return new MTETieredVoidMiner(this.mName, tier());
    }
}
