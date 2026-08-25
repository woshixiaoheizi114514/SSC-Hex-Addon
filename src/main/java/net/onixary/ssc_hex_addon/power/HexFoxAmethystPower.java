package net.onixary.ssc_hex_addon.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.mana.ManaRegistries;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.InstinctUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.ssc_hex_addon.SSCHexAddon;

public class HexFoxAmethystPower extends Power {

    private static final Identifier MODIFIER_ID = new Identifier(SSCHexAddon.MOD_ID, "amethyst_boost");

    // 每个阶段站在紫水晶母岩上时，媒介回复速度的增加量（每 tick 回复量）
    // 索引 0~3 对应 hex_fox_0 ~ hex_fox_3
    private static final double[] MEDIA_REGEN_ADD = { 0.0, 100.0, 400.0, 4000.0 };
    // 阶段0和1每 tick 增加的本能值（其他阶段无效）
    private static final float[] INSTINCT_PER_TICK = { 0.0f, 0.003f, 0.0f, 0.0f };

    private boolean active = false;

    public HexFoxAmethystPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking(); // 让 Power 每 tick 执行
    }

    @Override
    public void tick() {
        if (entity.getWorld().isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        boolean onAmethyst = player.getWorld().getBlockState(player.getBlockPos().down()).isOf(Blocks.BUDDING_AMETHYST);
        int stage = getStage(player);

        if (!onAmethyst || stage == -1) {
            removeBoost(player);
            return;
        }

        // 只在刚激活时添加一次修饰符，避免每 tick 重复叠加
        if (!active) {
            double regenAdd = MEDIA_REGEN_ADD[stage];
            ManaUtils.addRegenManaModifier(
                    player,
                    MODIFIER_ID,
                    ManaRegistries.MC_AlwaysTrue,
                    new ManaUtils.Modifier(regenAdd, 1.0d, 0.0d),
                    false
            );
            active = true;
        }

        // 本能增长（阶段0/1）
        float instinctGain = INSTINCT_PER_TICK[stage];
        if (instinctGain > 0f) {
            InstinctUtils.addInstinctEffect(
                    player,
                    new Identifier(SSCHexAddon.MOD_ID, "amethyst_instinct"),
                    instinctGain,
                    1,
                    false    // 持续效果，非瞬间
            );
        }
    }

    private int getStage(ServerPlayerEntity player) {
        PlayerFormComponent comp = PlayerFormComponent.COMPONENT.get(player);
        if (comp == null || comp.nowFormID == null) return -1;
        for (int i = 0; i < 4; i++) {
            if (comp.nowFormID.equals(SSCHexAddon.id("hex_fox_" + i))) {
                return i;
            }
        }
        return -1;
    }

    private void removeBoost(ServerPlayerEntity player) {
        if (active) {
            ManaUtils.removeRegenManaModifier(player, MODIFIER_ID, false);
            active = false;
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                new Identifier(SSCHexAddon.MOD_ID, "hex_fox_amethyst_boost"),
                new SerializableData(),
                data -> (type, entity) -> new HexFoxAmethystPower(type, entity)
        ).allowCondition();
    }
}