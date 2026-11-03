package com.jdz.vmd.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.jdz.vmd.common.fluids.LensColours;
import com.jdz.vmd.common.fluids.PlanetFluids;
import com.jdz.vmd.common.planets.PlanetSelection;
import com.jdz.vmd.common.tileentities.MTEVoidFluidDrill;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import gregtech.api.enums.Dyes;
import gregtech.api.util.GTUtility;
import gtneioreplugin.plugin.item.ItemDimensionDisplay;

/** NEI tab "Void Fluid Drill": one page per planet listing its fluids and the lens for each. */
public class PlanetFluidsHandler extends TemplateRecipeHandler {

    public static final String ID = "cvm.planetfluids";
    private static final int ROW = 19;

    public class Page extends CachedRecipe {

        final String abbr;
        final List<PlanetFluids.Entry> fluids;
        final PositionedStack planet;
        final List<PositionedStack> others = new ArrayList<>();

        Page(String abbr) {
            this.abbr = abbr;
            this.fluids = PlanetFluids.forAbbr(abbr);
            this.planet = new PositionedStack(ItemDimensionDisplay.getItem(abbr), 4, 16);
            for (int i = 0; i < fluids.size(); i++) {
                PlanetFluids.Entry e = fluids.get(i);
                int y = 16 + i * ROW;
                if (e.fluid() != null) {
                    others.add(new PositionedStack(GTUtility.getFluidDisplayStack(e.fluid()), 30, y));
                }
                ItemStack lens = LensColours.example(e.lens());
                if (lens != null) others.add(new PositionedStack(lens, 50, y));
            }
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> list = new ArrayList<>();
            list.add(planet);
            return list;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return others;
        }
    }

    @Override
    public String getRecipeName() {
        return "Void Fluid Drill";
    }

    @Override
    public String getOverlayIdentifier() {
        return ID;
    }

    @Override
    public String getGuiTexture() {
        return "textures/gui/container/furnace.png";
    }

    @Override
    public void drawBackground(int recipe) {}

    @Override
    public int getRecipeHeight(int recipe) {
        Page page = (Page) arecipes.get(recipe);
        return 36 + Math.max(1, page.fluids.size()) * ROW;
    }

    @Override
    public void drawExtras(int recipe) {
        Page page = (Page) arecipes.get(recipe);
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRenderer.drawString(MTEVoidFluidDrill.planetName(page.abbr), 4, 2, 0x404040);
        for (int i = 0; i < page.fluids.size(); i++) {
            PlanetFluids.Entry e = page.fluids.get(i);
            int y = 16 + i * ROW;
            mc.fontRenderer.drawString(e.displayName(), 70, y, 0x202020);
            mc.fontRenderer.drawString(
                LensColours.displayName(e.lens()) + " lens, " + Math.round(e.share * 100) + "% without a lens",
                70,
                y + 9,
                0x707070);
        }
        int y = 20 + Math.max(1, page.fluids.size()) * ROW;
        mc.fontRenderer.drawString(
            EnumChatFormatting.DARK_GRAY + "Planet block in the controller, lens in an input bus.",
            4,
            y,
            0x606060);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (ID.equals(outputId)) {
            for (String abbr : PlanetFluids.planetsWithFluids()) arecipes.add(new Page(abbr));
        } else super.loadCraftingRecipes(outputId, results);
    }

    /** Looking up a fluid (or a container of it) shows the planets it can be drilled on. */
    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack fluid = GTUtility.getFluidFromDisplayStack(result);
        if (fluid == null) fluid = GTUtility.getFluidForFilledItem(result, true);
        if (fluid == null || fluid.getFluid() == null) return;
        String name = fluid.getFluid()
            .getName();
        for (String abbr : PlanetFluids.planetsWithFluids()) {
            for (PlanetFluids.Entry e : PlanetFluids.forAbbr(abbr)) {
                if (e.registry.equals(name)) {
                    arecipes.add(new Page(abbr));
                    break;
                }
            }
        }
    }

    /** Uses of a planet block, a lens, or the drill itself. */
    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        String planet = PlanetSelection.abbrOf(ingredient);
        if (planet != null) {
            if (!PlanetFluids.forAbbr(planet)
                .isEmpty()) arecipes.add(new Page(planet));
            return;
        }
        Dyes lens = LensColours.colourOf(ingredient);
        if (lens != null) {
            for (String abbr : PlanetFluids.planetsWithFluids()) {
                if (PlanetFluids.forLens(PlanetFluids.forAbbr(abbr), lens) != null) arecipes.add(new Page(abbr));
            }
            return;
        }
        if (isDrill(ingredient)) loadCraftingRecipes(ID);
    }

    private static boolean isDrill(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        try {
            if (!"gregtech:gt.blockmachines".equals(
                String.valueOf(net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem())))) return false;
            int meta = stack.getItemDamage();
            return meta >= 0 && meta < gregtech.api.GregTechAPI.METATILEENTITIES.length
                && gregtech.api.GregTechAPI.METATILEENTITIES[meta] instanceof MTEVoidFluidDrill;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
