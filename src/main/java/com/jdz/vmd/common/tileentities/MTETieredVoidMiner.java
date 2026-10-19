package com.jdz.vmd.common.tileentities;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import com.jdz.vmd.Config;
import com.jdz.vmd.common.planets.PlanetSelection;
import com.jdz.vmd.common.planets.PlanetSurface;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import bwcrossmod.galacticgreg.MTEVoidMiners.VMLUV;
import bwcrossmod.galacticgreg.VoidMinerUtility;
import galacticgreg.api.enums.DimensionDef;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtneioreplugin.util.DimensionHelper;
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
            .addInfo("Put a planet block (GT NEI Ore Plugin dimension display) in the controller slot to mine")
            .addInfo("that planet instead of the dimension the machine stands in.")
            .addInfo(
                Config.planetTierGating
                    ? "Accepts planets up to T" + Config.maxPlanetTier[t.ordinal()] + "."
                    : "Accepts planets of any tier.")
            .addInfo("Sneak + screwdriver: switch between Ore mode and Surface mode.")
            .addInfo("Surface mode mines the planet's stone, cobblestone and topsoil instead of ores.")
            .addInfo("Screwdriver: toggle the ore filter between whitelist and blacklist.")
            .addInfo("Requires " + t.name() + " or better energy; faster hatches do not upgrade output.")
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

    // ---------------------------------------------------------------------------------------------------------
    // Planet selection and modes
    // ---------------------------------------------------------------------------------------------------------

    private static final String NOT_APPLIED = "\u0000";
    private boolean surfaceMode;
    private String appliedPlanet = NOT_APPLIED;
    private List<ItemStack> surfaceBlocks = Collections.emptyList();

    private static Field fTotalWeight, fCanVoidMine;
    private static Method mCalculateDropMap;

    private static void initReflection() throws ReflectiveOperationException {
        if (fTotalWeight != null) return;
        Field tw = MTEVoidMinerBase.class.getDeclaredField("totalWeight");
        Field cv = MTEVoidMinerBase.class.getDeclaredField("canVoidMine");
        Method cd = MTEVoidMinerBase.class.getDeclaredMethod("calculateDropMap");
        tw.setAccessible(true);
        cv.setAccessible(true);
        cd.setAccessible(true);
        mCalculateDropMap = cd;
        fCanVoidMine = cv;
        fTotalWeight = tw;
    }

    /** The planet in the controller slot (ore-plugin abbreviation), or null to use the machine's own dimension. */
    private String selectedPlanet() {
        return PlanetSelection.abbrOf(getControllerSlot());
    }

    private String effectiveAbbr(String planet) {
        if (planet != null) return planet;
        try {
            String dimName = DimensionDef.getDimensionName(getBaseMetaTileEntity().getWorld());
            return dimName == null ? null : DimensionHelper.INTERNAL_TO_ABBR.get(dimName);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Points the inherited ore tables at the selected planet, or back at the machine's own dimension. */
    private void applyPlanet(String planet) {
        try {
            initReflection();
            if (planet == null) {
                mCalculateDropMap.invoke(this);
            } else {
                String internal = DimensionHelper.ABBR_TO_INTERNAL.get(planet);
                dropMap = VoidMinerUtility.dropMapsByDimName.getOrDefault(internal, new VoidMinerUtility.DropMap());
                extraDropMap = VoidMinerUtility.extraDropsByDimName
                    .getOrDefault(internal, new VoidMinerUtility.DropMap());
                dropMap.isDistributionCached(extraDropMap);
                fCanVoidMine.setBoolean(this, true);
                fTotalWeight.setFloat(this, dropMap.getTotalWeight() + extraDropMap.getTotalWeight());
            }
            if (dropMap != null && selected.getSlots() != dropMap.getOres().length) {
                selected.setSize(dropMap.getOres().length);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("[CheaperVoidMiners] Void Miner internals changed", e);
        }
        surfaceBlocks = PlanetSurface.forAbbr(effectiveAbbr(planet));
        appliedPlanet = planet == null ? "" : planet;
    }

    @Override
    public CheckRecipeResult checkProcessing() {
        String planet = selectedPlanet();
        if (planet != null && Config.planetTierGating) {
            int planetTier = PlanetSelection.tierOf(PlanetSelection.byAbbr(planet));
            if (planetTier > Config.maxPlanetTier[tier().ordinal()]) {
                stopMachine(ShutDownReasonRegistry.NONE);
                return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("cvm_planet_tier_too_high");
            }
        }
        String key = planet == null ? "" : planet;
        if (!key.equals(appliedPlanet)) applyPlanet(planet);

        if (surfaceMode) {
            setElectricityStats();
            if (surfaceBlocks.isEmpty()) {
                stopMachine(ShutDownReasonRegistry.NONE);
                return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("cvm_no_surface_blocks");
            }
            return SimpleCheckRecipeResult.ofSuccess("cvm_surface_mining");
        }
        return super.checkProcessing();
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        if (aPlayer.isSneaking()) {
            surfaceMode = !surfaceMode;
            aPlayer.addChatComponentMessage(
                new ChatComponentText(
                    EnumChatFormatting.GOLD + "Void Miner mode: "
                        + EnumChatFormatting.WHITE
                        + (surfaceMode ? "Surface (planet stone and topsoil)" : "Ore")));
            return;
        }
        super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("cvmSurfaceMode", surfaceMode);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        surfaceMode = aNBT.getBoolean("cvmSurfaceMode");
    }

    @Override
    protected void setElectricityStats() {
        super.setElectricityStats();
        mEUt = -Math.abs(Math.toIntExact(GTValues.VP[tier().voltage]));
    }

    @Override
    protected void outputAfterRecipe() {
        if (surfaceMode) {
            if (!surfaceBlocks.isEmpty()) {
                int batches = Math.max(1, mMaxProgresstime / 10);
                int runs = tier().outputRuns * batches;
                for (int i = 0; i < runs; i++) {
                    ItemStack pick = surfaceBlocks.get(ThreadLocalRandom.current().nextInt(surfaceBlocks.size()))
                        .copy();
                    pick.stackSize = Config.surfaceBlocksPerRun;
                    addOutputPartial(pick);
                }
                updateSlots();
            }
            return;
        }
        // Native GTNH generation is used for each output sample (planet-specific when a planet is selected).
        for (int i = 0; i < tier().outputRuns; i++) {
            super.outputAfterRecipe();
        }
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity base) {
        return new MTETieredVoidMiner(this.mName, tier());
    }
}
