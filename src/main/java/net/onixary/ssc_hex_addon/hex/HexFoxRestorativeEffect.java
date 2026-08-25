package net.onixary.ssc_hex_addon.hex;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.InstinctUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.ssc_hex_addon.SSCHexAddon;

import java.util.HashMap;
import java.util.Map;

public class HexFoxRestorativeEffect extends StatusEffect {
    private static final double[] MEDIA_AMOUNTS = {10000.0, 20000.0, 40000.0, 200000.0};
    private static final double[] INSTINCT_TOTALS = {1.0, 0.5, 0.0, 0.0};
    private static final int DURATION_TICKS = 20;

    private static final Map<Identifier, Integer> FORM_STAGE_MAP = new HashMap<>();
    static {
        FORM_STAGE_MAP.put(SSCHexAddon.id("hex_fox_0"), 0);
        FORM_STAGE_MAP.put(SSCHexAddon.id("hex_fox_1"), 1);
        FORM_STAGE_MAP.put(SSCHexAddon.id("hex_fox_2"), 2);
        FORM_STAGE_MAP.put(SSCHexAddon.id("hex_fox_3"), 3);
    }

    public HexFoxRestorativeEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xDBA4FF);  // 亮紫色，无alpha
    }

    @Override
    public int getColor() {
        return 0xDBA4FF;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (entity.getWorld().isClient) return;

        StatusEffectInstance instance = player.getStatusEffect(this);
        if (instance == null) return;

        PlayerFormComponent comp = PlayerFormComponent.COMPONENT.get(player);
        if (comp == null || comp.nowFormID == null) {
            player.removeStatusEffect(this);
            return;
        }

        Integer stage = FORM_STAGE_MAP.get(comp.nowFormID);
        if (stage == null) {
            player.removeStatusEffect(this);
            return;
        }

        double mediaPerTick = MEDIA_AMOUNTS[stage] / DURATION_TICKS;
        double instinctTotal = INSTINCT_TOTALS[stage];
        float instinctPerTick = (float) (instinctTotal / DURATION_TICKS);

        ManaUtils.gainPlayerManaWithTime(player, mediaPerTick, DURATION_TICKS);
        InstinctUtils.addInstinctEffect(
                player,
                new Identifier(SSCHexAddon.MOD_ID, "hex_fox_restorative"),
                instinctPerTick,
                DURATION_TICKS,
                false
        );

        player.removeStatusEffect(this);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}