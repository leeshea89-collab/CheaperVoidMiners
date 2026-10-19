package com.jdz.vmd.common.planets;

import net.minecraft.item.ItemStack;

import gtneioreplugin.plugin.item.ItemDimensionDisplay;
import gtneioreplugin.util.DimensionHelper;

/** Reads a GT NEI Ore Plugin planet block ("dimension display") placed in a controller slot. */
public final class PlanetSelection {

    private PlanetSelection() {}

    /** Ore-plugin abbreviation of the planet block in the stack, or null if it is not a planet block. */
    public static String abbrOf(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        if (!(stack.getItem() instanceof ItemDimensionDisplay)) return null;
        try {
            String abbr = ItemDimensionDisplay.getDimension(stack);
            return byAbbr(abbr) == null ? null : abbr;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static DimensionHelper.Dimension byAbbr(String abbr) {
        if (abbr == null) return null;
        for (DimensionHelper.Dimension d : DimensionHelper.ALL_DIMENSIONS) {
            if (abbr.equals(d.abbr())) return d;
        }
        return null;
    }

    /** Planet tier 0-10 from the ore plugin's tier key ("gtnop.tier.N"). */
    public static int tierOf(DimensionHelper.Dimension dim) {
        String key = dim.tierKey();
        try {
            return Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
