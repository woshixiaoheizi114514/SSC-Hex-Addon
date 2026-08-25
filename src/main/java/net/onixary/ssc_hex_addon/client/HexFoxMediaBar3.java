package net.onixary.ssc_hex_addon.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.onixary.shapeShifterCurseFabric.mana.IManaRender;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import static net.onixary.ssc_hex_addon.SSCHexAddon.MOD_ID;

@Environment(EnvType.CLIENT)
public class HexFoxMediaBar3 implements IManaRender {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Identifier BAR_FULL = new Identifier(MOD_ID, "textures/gui/hex_fox_media_bar_full.png");
    private static final Identifier BAR_EMPTY = new Identifier(MOD_ID, "textures/gui/hex_fox_media_bar_empty.png");

    @Override
    public boolean OverrideInstinctBar() {
        return false;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!mc.options.hudHidden) {
            Pair<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(
                ShapeShifterCurseFabric.clientConfig.manaBarPosType,
                ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetX,
                ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetY
            );
            renderBar(context, tickDelta, pos.getLeft(), pos.getRight());
        }
    }

    private void renderBar(DrawContext context, float tickDelta, int x, int y) {
        double fillPercent = ManaUtils.getPlayerManaPercent(mc.player, 0.0f);
        int fillWidth = (int) Math.ceil(80 * fillPercent);
        context.drawTexture(BAR_EMPTY, x, y, 0, 0, 80, 5, 80, 5);
        context.drawTexture(BAR_FULL, x, y, 0, 0, fillWidth, 5, 80, 5);
    }
}