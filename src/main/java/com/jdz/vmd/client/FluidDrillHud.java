package com.jdz.vmd.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.jdz.vmd.Config;
import com.jdz.vmd.common.fluids.LensColours;
import com.jdz.vmd.common.fluids.PlanetFluids;
import com.jdz.vmd.common.planets.PlanetSelection;
import com.jdz.vmd.common.tileentities.MTEVoidFluidDrill;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import gregtech.api.enums.Dyes;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gtneioreplugin.util.DimensionHelper;

/**
 * On-screen panel (right side, middle) listing a planet's fluids with the lens that selects each one. Shown while
 * looking at a Void Fluid Drill controller, or while holding a planet block.
 */
public final class FluidDrillHud {

    private static final int ROW = 18;
    private final KeyBinding toggle = new KeyBinding("key.cvm.hud", Keyboard.KEY_NONE, "key.categories.cvm");
    private final RenderItem itemRender = new RenderItem();
    private boolean visible = true;

    public FluidDrillHud() {
        ClientRegistry.registerKeyBinding(toggle);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (toggle.isPressed()) visible = !visible;
    }

    private static final class Line {

        final ItemStack icon;
        final String text;

        Line(ItemStack icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !visible) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo || mc.thePlayer == null) return;
        List<Line> lines;
        try {
            lines = build(mc);
        } catch (RuntimeException e) {
            return;
        }
        if (lines == null || lines.isEmpty()) return;
        draw(mc, event.resolution, lines);
    }

    private List<Line> build(Minecraft mc) {
        MTEVoidFluidDrill drill = lookedAtDrill(mc);
        String abbr;
        Dyes lens = null;
        String title;
        List<PlanetFluids.Entry> fluids;
        List<Line> out = new ArrayList<>();
        if (drill != null) {
            DimensionHelper.Dimension d = drill.clientPlanet >= 0 ? DimensionHelper.getByIndex(drill.clientPlanet)
                : null;
            abbr = d != null ? d.abbr() : null;
            lens = drill.clientLens >= 0 && drill.clientLens < Dyes.VALUES.length ? Dyes.VALUES[drill.clientLens]
                : null;
            if (abbr != null) {
                fluids = PlanetFluids.forAbbr(abbr);
                title = drill.tier()
                    .name() + " Void Fluid Drill: " + MTEVoidFluidDrill.planetName(abbr);
            } else {
                fluids = PlanetFluids.forWorld(mc.theWorld);
                String here = PlanetFluids.abbrOfWorld(mc.theWorld);
                title = drill.tier()
                    .name() + " Void Fluid Drill: "
                    + (here != null ? MTEVoidFluidDrill.planetName(here) : "this dimension");
            }
            out.add(new Line(null, EnumChatFormatting.GOLD + title));
            if (drill.clientStatus == MTEVoidFluidDrill.STATUS_TIER_TOO_HIGH) {
                out.add(
                    new Line(
                        null,
                        EnumChatFormatting.RED + (drill.maxPlanetTier() < 0 ? "Needs at least "
                            + gregtech.api.enums.GTValues.VN[Config.fluidPlanetFirstVoltage] + " power"
                            : "Planet tier too high: this voltage reaches T" + drill.maxPlanetTier())));
            }
        } else {
            ItemStack held = mc.thePlayer.getHeldItem();
            abbr = PlanetSelection.abbrOf(held);
            if (abbr == null) return null;
            fluids = PlanetFluids.forAbbr(abbr);
            out.add(new Line(null, EnumChatFormatting.GOLD + MTEVoidFluidDrill.planetName(abbr) + " fluids"));
            int t = PlanetSelection.tierOf(PlanetSelection.byAbbr(abbr));
            out.add(new Line(null, EnumChatFormatting.GRAY + "Void Fluid Drill: " + minDrillFor(t)));
        }

        if (fluids.isEmpty()) {
            out.add(new Line(null, EnumChatFormatting.RED + "No underground fluids here"));
            return out;
        }
        for (PlanetFluids.Entry e : fluids) {
            boolean selected = lens != null && e.lens() == lens;
            StringBuilder sb = new StringBuilder();
            sb.append(selected ? EnumChatFormatting.GREEN + "> " : EnumChatFormatting.WHITE.toString());
            sb.append(e.displayName());
            sb.append(EnumChatFormatting.GRAY)
                .append("  ")
                .append(Math.round(e.share * 100))
                .append("%");
            if (drill != null) {
                sb.append(", ")
                    .append(
                        MTEVoidFluidDrill.litresPerSecond(
                            e,
                            drill.tier(),
                            drill.clientVoltage >= 0 ? drill.clientVoltage : drill.tier().voltage,
                            true))
                    .append(" L/s");
            }
            sb.append(EnumChatFormatting.DARK_GRAY)
                .append("  (")
                .append(LensColours.displayName(e.lens()))
                .append(" lens)");
            out.add(new Line(LensColours.example(e.lens()), sb.toString()));
        }
        if (drill != null) {
            if (lens == null) {
                out.add(
                    new Line(
                        null,
                        EnumChatFormatting.YELLOW + "No lens: random fluid at "
                            + Math.round(Config.noLensFactor * 100)
                            + "% rate"));
            } else if (PlanetFluids.forLens(fluids, lens) == null) {
                out.add(
                    new Line(
                        LensColours.example(lens),
                        EnumChatFormatting.RED + lens.mName + " lens matches nothing here"));
            }
        } else {
            out.add(new Line(null, EnumChatFormatting.DARK_GRAY + "Lens goes in the drill's input bus"));
        }
        return out;
    }

    private static String minDrillFor(int planetTier) {
        if (!Config.planetTierGating) return "any tier";
        int v = MTEVoidFluidDrill.voltageFor(planetTier);
        if (v < 0) return "no voltage reaches T" + planetTier;
        return gregtech.api.enums.GTValues.VN[v] + " power or better";
    }

    private static MTEVoidFluidDrill lookedAtDrill(Minecraft mc) {
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return null;
        TileEntity te = mc.theWorld.getTileEntity(hit.blockX, hit.blockY, hit.blockZ);
        if (!(te instanceof IGregTechTileEntity gte)) return null;
        return gte.getMetaTileEntity() instanceof MTEVoidFluidDrill d ? d : null;
    }

    private void draw(Minecraft mc, ScaledResolution res, List<Line> lines) {
        FontRenderer font = mc.fontRenderer;
        int width = 0;
        for (Line l : lines) width = Math.max(width, font.getStringWidth(l.text) + (l.icon != null ? 20 : 0));
        int height = 0;
        for (Line l : lines) height += l.icon != null ? ROW : 11;
        int x = res.getScaledWidth() - width - 10;
        int y = (res.getScaledHeight() - height) / 2;

        Gui.drawRect(x - 4, y - 4, x + width + 4, y + height + 2, 0xB0101018);
        int cy = y;
        for (Line l : lines) {
            if (l.icon != null) {
                drawItem(mc, l.icon, x, cy);
                font.drawStringWithShadow(l.text, x + 20, cy + 4, 0xFFFFFF);
                cy += ROW;
            } else {
                font.drawStringWithShadow(l.text, x, cy, 0xFFFFFF);
                cy += 11;
            }
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawItem(Minecraft mc, ItemStack stack, int x, int y) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        try {
            itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, x, y);
        } catch (RuntimeException ignored) {}
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
