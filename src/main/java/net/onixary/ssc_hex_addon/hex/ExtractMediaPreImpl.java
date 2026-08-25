package net.onixary.ssc_hex_addon.hex;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.ssc_hex_addon.SSCHexAddon;

public class ExtractMediaPreImpl implements CastingEnvironmentComponent.ExtractMedia.Pre {
    private final PlayerBasedCastEnv playerEnv;
    private static final CastingEnvironmentComponent.Key<ExtractMediaPreImpl> KEY = new CastingEnvironmentComponent.Key<>() {};

    public ExtractMediaPreImpl(PlayerBasedCastEnv playerEnv) {
        this.playerEnv = playerEnv;
    }

    @Override
    public long onExtractMedia(long cost, boolean simulate) {
        ServerPlayerEntity player = playerEnv.getCaster();
        if (player == null) return cost;

        // 检查是否有任意 hex_fox 媒介
        boolean hasHexFoxMedia = false;
        for (Identifier id : new Identifier[]{
            SSCHexAddon.HEX_FOX_MEDIA_0,
            SSCHexAddon.HEX_FOX_MEDIA_1,
            SSCHexAddon.HEX_FOX_MEDIA_2,
            SSCHexAddon.HEX_FOX_MEDIA_3
        }) {
            if (ManaUtils.isManaTypeExists(player, id, null)) {
                hasHexFoxMedia = true;
                break;
            }
        }
        if (!hasHexFoxMedia) return cost;

        double currentMana = ManaUtils.getPlayerMana(player);
        double toConsume = Math.min(currentMana, (double) cost);

        if (!simulate) {
            ManaUtils.setPlayerMana(player, currentMana - toConsume);
        }

        return cost - (long) toConsume;
    }

    @Override
    public CastingEnvironmentComponent.Key<?> getKey() {
        return KEY;
    }
}