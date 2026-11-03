package com.jdz.vmd.common.tileentities;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ORE_DRILL;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ORE_DRILL_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ORE_DRILL_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ORE_DRILL_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.getCasingTextureForId;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.jdz.vmd.Config;
import com.jdz.vmd.common.fluids.LensColours;
import com.jdz.vmd.common.fluids.PlanetFluids;
import com.jdz.vmd.common.planets.PlanetSelection;
import com.jdz.vmd.common.tileentities.MTETieredVoidMiner.Tier;

import gregtech.api.casing.Casings;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtneioreplugin.util.DimensionHelper;

/**
 * Void Fluid Drill (LV-IV): pumps a planet's underground fluids without depleting anything.
 * Planet = planet block in the controller slot (else the dimension it stands in); fluid = the lens colour in an input
 * bus (or the controller slot when no planet block is used). No lens: a random fluid of the planet at a lower rate.
 */
public class MTEVoidFluidDrill extends MTEEnhancedMultiBlockBase<MTEVoidFluidDrill> implements ISurvivalConstructable {

    private static final String MAIN = "main";
    /** Client sync events (GT's own ids are below 20). */
    public static final byte EVENT_PLANET = 100, EVENT_LENS = 101, EVENT_STATUS = 102;
    public static final int STATUS_OK = 0, STATUS_TIER_TOO_HIGH = 1, STATUS_NO_FLUID_FOR_LENS = 2,
        STATUS_NO_FLUIDS = 3;

    private static final class Structures {

        static final List<IStructureDefinition<MTEVoidFluidDrill>> DEFINITIONS = createAll();
    }

    private static List<IStructureDefinition<MTEVoidFluidDrill>> createAll() {
        List<IStructureDefinition<MTEVoidFluidDrill>> result = new ArrayList<>();
        for (Tier tier : Tier.values()) result.add(build(tier));
        return result;
    }

    // Same footprint as the Void Miners (so it builds the same way), with the drill string as pipe casings.
    private static IStructureDefinition<MTEVoidFluidDrill> build(Tier tier) {
        Casings primary = tier.primary();
        return StructureDefinition.<MTEVoidFluidDrill>builder()
            // spotless:off
            .addShape(
                MAIN,
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
            .addElement('C', tier.pipe()
                .asElement())
            .addElement('D', tier.secondary()
                .asElement())
            .addElement(
                'E',
                buildHatchAdder(MTEVoidFluidDrill.class).atLeast(OutputHatch, InputBus, Maintenance, Energy)
                    .hint(1)
                    .casingIndex(primary.getTextureId())
                    .buildAndChain(primary.asElement()))
            .build();
    }

    private final Tier selectedTier;

    // Server-side state of the last cycle, and the client copies sent by block events.
    private int status = STATUS_OK;
    public int clientPlanet = -1, clientLens = -1, clientStatus = STATUS_OK;

    public MTEVoidFluidDrill(int id, String name, String localName, Tier tier) {
        super(id, name, localName);
        selectedTier = tier;
    }

    private MTEVoidFluidDrill(String name, Tier tier) {
        super(name);
        selectedTier = tier;
    }

    public Tier tier() {
        return selectedTier == null ? Tier.LV : selectedTier;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity base) {
        return new MTEVoidFluidDrill(mName, tier());
    }

    @Override
    public IStructureDefinition<MTEVoidFluidDrill> getStructureDefinition() {
        return Structures.DEFINITIONS.get(tier().ordinal());
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(MAIN, stackSize, hintsOnly, 3, 7, 1);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(MAIN, stackSize, 3, 7, 1, elementBudget, env, false, true);
    }

    @Override
    public void checkMachine(IGregTechTileEntity base, ItemStack stack, List<StructureError> errors) {
        if (!checkPiece(MAIN, 3, 7, 1, errors)) return;
        checkHasEnergyHatch(errors);
        checkOneMaintenanceHatch(errors);
        checkHasOutputHatch(errors);
        if (GTUtility.getTier(getMaxInputVoltage()) < tier().voltage) {
            errors.add(StructureErrors.energyHatchTierTooLow(tier().voltage));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Running
    // ---------------------------------------------------------------------------------------------------------

    /** Highest planet tier this drill may target with a planet block. */
    public int maxPlanetTier() {
        return Config.maxPlanetTier[tier().ordinal()];
    }

    /** Litres per second for a fluid at this tier, with or without a lens. */
    public static int litresPerSecond(PlanetFluids.Entry e, Tier tier, boolean lens) {
        double rate = e.maxAmount * Config.fluidRateFactor[tier.ordinal()] * (lens ? 1.0 : Config.noLensFactor);
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE / 40, Math.round(rate)));
    }

    private Dyes findLens(boolean planetInController) {
        if (!planetInController) {
            Dyes c = LensColours.colourOf(getControllerSlot());
            if (c != null) return c;
        }
        for (ItemStack s : getStoredInputs()) {
            Dyes c = LensColours.colourOf(s);
            if (c != null) return c;
        }
        return null;
    }

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        String planet = PlanetSelection.abbrOf(getControllerSlot());
        Dyes lens = findLens(planet != null);
        sync(planet, lens);

        if (planet != null && Config.planetTierGating
            && PlanetSelection.tierOf(PlanetSelection.byAbbr(planet)) > maxPlanetTier()) {
            return fail(STATUS_TIER_TOO_HIGH, "cvm_planet_tier_too_high");
        }
        List<PlanetFluids.Entry> fluids = planet != null ? PlanetFluids.forAbbr(planet)
            : PlanetFluids.forWorld(getBaseMetaTileEntity().getWorld());
        if (fluids.isEmpty()) return fail(STATUS_NO_FLUIDS, "cvm_no_planet_fluids");

        PlanetFluids.Entry pick = lens != null ? PlanetFluids.forLens(fluids, lens)
            : PlanetFluids.random(fluids, ThreadLocalRandom.current());
        if (pick == null) return fail(STATUS_NO_FLUID_FOR_LENS, "cvm_no_fluid_for_lens");
        Fluid fluid = pick.fluid();
        if (fluid == null) return fail(STATUS_NO_FLUIDS, "cvm_no_planet_fluids");

        status = STATUS_OK;
        mOutputItems = null;
        mOutputFluids = new FluidStack[] { new FluidStack(fluid, litresPerSecond(pick, tier(), lens != null)) };
        mEUt = -Math.abs(Math.toIntExact(GTValues.VP[tier().voltage]));
        mMaxProgresstime = 20;
        mEfficiency = getCurrentEfficiency(null);
        mEfficiencyIncrease = 10000;
        return SimpleCheckRecipeResult.ofSuccess("cvm_fluid_drilling");
    }

    private CheckRecipeResult fail(int newStatus, String key) {
        status = newStatus;
        sendStatus();
        stopMachine(ShutDownReasonRegistry.NONE);
        return SimpleCheckRecipeResult.ofFailurePersistOnShutdown(key);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Client sync for the heads-up display
    // ---------------------------------------------------------------------------------------------------------

    private int lastPlanet = Integer.MIN_VALUE, lastLens = Integer.MIN_VALUE;

    private void sync(String planet, Dyes lens) {
        int p = planet == null ? -1 : DimensionHelper.getIndexByAbbr(planet);
        int l = lens == null ? -1 : lens.mIndex;
        if (p == lastPlanet && l == lastLens) return;
        lastPlanet = p;
        lastLens = l;
        sendState();
    }

    private void sendState() {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base == null || !base.isServerSide()) return;
        base.sendBlockEvent(EVENT_PLANET, (byte) (lastPlanet == Integer.MIN_VALUE ? 0 : lastPlanet + 1));
        base.sendBlockEvent(EVENT_LENS, (byte) (lastLens == Integer.MIN_VALUE ? 0 : lastLens + 1));
        sendStatus();
    }

    private void sendStatus() {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base != null && base.isServerSide()) base.sendBlockEvent(EVENT_STATUS, (byte) status);
    }

    @Override
    public void onPostTick(IGregTechTileEntity base, long tick) {
        super.onPostTick(base, tick);
        if (!base.isServerSide() || tick % 40 != 7) return;
        // Re-read the slots while idle too, and resend so players who arrive later see the state.
        String planet = PlanetSelection.abbrOf(getControllerSlot());
        Dyes lens = findLens(planet != null);
        int p = planet == null ? -1 : DimensionHelper.getIndexByAbbr(planet);
        lastPlanet = p;
        lastLens = lens == null ? -1 : lens.mIndex;
        if (planet != null && Config.planetTierGating
            && PlanetSelection.tierOf(PlanetSelection.byAbbr(planet)) > maxPlanetTier()) {
            status = STATUS_TIER_TOO_HIGH;
        } else if (status == STATUS_TIER_TOO_HIGH) {
            status = STATUS_OK;
        }
        sendState();
    }

    @Override
    public void receiveClientEvent(byte id, byte value) {
        super.receiveClientEvent(id, value);
        switch (id) {
            case EVENT_PLANET -> clientPlanet = (value & 0xFF) - 1;
            case EVENT_LENS -> clientLens = (value & 0xFF) - 1;
            case EVENT_STATUS -> clientStatus = value;
            default -> {}
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Info
    // ---------------------------------------------------------------------------------------------------------

    @Override
    public String[] getInfoData() {
        List<String> lines = new ArrayList<>(Arrays.asList(super.getInfoData()));
        String planet = PlanetSelection.abbrOf(getControllerSlot());
        Dyes lens = findLens(planet != null);
        String where = planet != null ? planetName(planet)
            : "this dimension (no planet block in the controller)";
        lines.add(EnumChatFormatting.GOLD + "Void Fluid Drill: " + EnumChatFormatting.WHITE + where);
        lines.add("Lens: " + (lens == null ? "none (random fluid at reduced rate)" : lens.mName));
        List<PlanetFluids.Entry> fluids = planet != null ? PlanetFluids.forAbbr(planet)
            : PlanetFluids.forWorld(getBaseMetaTileEntity().getWorld());
        if (fluids.isEmpty()) lines.add(EnumChatFormatting.RED + "No underground fluids known here.");
        for (PlanetFluids.Entry e : fluids) {
            lines.add(
                (lens != null && e.lens() == lens ? EnumChatFormatting.GREEN + "> " : "  ") + e.displayName()
                    + EnumChatFormatting.GRAY
                    + " - "
                    + LensColours.displayName(e.lens())
                    + " lens, "
                    + Math.round(e.share * 100)
                    + "%, "
                    + litresPerSecond(e, tier(), true)
                    + " L/s");
        }
        return lines.toArray(new String[0]);
    }

    public static String planetName(String abbr) {
        DimensionHelper.Dimension d = PlanetSelection.byAbbr(abbr);
        if (d == null) return abbr;
        String full = d.fullName();
        try {
            full = DimensionHelper.getDimLocalizedName(d.internalName());
        } catch (RuntimeException ignored) {}
        return full + " (T" + PlanetSelection.tierOf(d) + ")";
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        Tier t = tier();
        return new MultiblockTooltipBuilder().addMachineType(t.name() + " Void Fluid Drill")
            .addInfo("Pumps a planet's underground fluids from the void. Nothing runs dry.")
            .addInfo("Put a planet block (GT NEI Ore Plugin dimension display) in the controller slot to")
            .addInfo("drill that planet; without one it drills the dimension it stands in.")
            .addInfo(
                Config.planetTierGating ? "Accepts planets up to T" + Config.maxPlanetTier[t.ordinal()] + "."
                    : "Accepts planets of any tier.")
            .addInfo("Put a coloured lens in an input bus to choose the fluid. Each fluid on a planet")
            .addInfo("has its own lens colour; any lens of that colour works and is never used up.")
            .addInfo(
                "No lens: a random fluid of the planet at " + Math.round(Config.noLensFactor * 100)
                    + "% of the rate.")
            .addInfo("Look at the controller or hold a planet block to see the fluids and their lenses.")
            .addInfo(
                "Rate: the fluid's GT max amount x " + Config.fluidRateFactor[t.ordinal()]
                    + " L/s. Uses "
                    + GTValues.VP[t.voltage]
                    + " EU/t.")
            .beginStructureBlock(7, 9, 7, false)
            .addController("Front centre, second layer")
            .addCasing("42", t.frameLabel + " Frame Boxes", false)
            .addCasing("12-17", t.primaryLabel, false)
            .addCasing("6", t.pipeLabel + " (drill string)", false)
            .addCasing("6", t.secondaryLabel, false)
            .addEnergyHatch("1+", "Leg casing (" + t.name() + "+)", 1)
            .addMaintenanceHatch("1", "Leg casing", 1)
            .addOutputHatch("1+", "Leg casing", 1)
            .addInputBus("0+", "Leg casing (lens)", 1)
            .toolTipFinisher();
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity base, ForgeDirection side, ForgeDirection facing, int colorIndex,
        boolean active, boolean redstoneLevel) {
        int casing = tier().primary()
            .getTextureId();
        if (side == facing) {
            return new ITexture[] { getCasingTextureForId(casing), TextureFactory.builder()
                .addIcon(active ? OVERLAY_FRONT_ORE_DRILL_ACTIVE : OVERLAY_FRONT_ORE_DRILL)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(active ? OVERLAY_FRONT_ORE_DRILL_ACTIVE_GLOW : OVERLAY_FRONT_ORE_DRILL_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { getCasingTextureForId(casing) };
    }

    @Override
    public int getMaxEfficiency(ItemStack stack) {
        return 10000;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack stack) {
        return true;
    }

    @Override
    public int getDamageToComponent(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack stack) {
        return false;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return false;
    }
}
