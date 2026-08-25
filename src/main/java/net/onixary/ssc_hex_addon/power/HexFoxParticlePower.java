package net.onixary.ssc_hex_addon.power;

import at.petrak.hexcasting.api.pigment.ColorProvider;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.onixary.ssc_hex_addon.SSCHexAddon;

import java.util.Random;

public class HexFoxParticlePower extends Power {
    private static final Random RANDOM = new Random();

    // ===== 粒子频率 =====
    // 每 20 tick（1 秒）生成一次粒子。
    // 想改频率就改这个数字：
    //  - 10 = 每 0.5 秒一次（更快）
    //  - 40 = 每 2 秒一次（更慢）
    private static final int PARTICLE_INTERVAL = 20;

    public HexFoxParticlePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking(); // 让这个 Power 每 tick 执行
    }

    @Override
    public void tick() {
        if (entity.getWorld().isClient) return; // 只在服务端生成粒子
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (player.age % PARTICLE_INTERVAL != 0) return;

        // ===== 默认颜色 =====
        // 0xFFAA00FF 是一个紫色（AARRGGBB 格式，前两位为透明度）。
        // 如果玩家有自定义颜料，会使用玩家的颜料覆盖默认值。
        int color = 0xFFAA00FF;

        FrozenPigment pigment = IXplatAbstractions.INSTANCE.getPigment(player);
        if (pigment != null) {
            ColorProvider colorProvider = pigment.getColorProvider();
            float time = player.age;
            Vec3d pos = player.getPos();
            color = colorProvider.getColor(time, pos);
        }

        // ===== 粒子偏移 =====
        // 以下三个坐标决定粒子生成的范围（相对于玩家位置）。
        // 每个公式末尾的 "* 1" 控制偏移幅度（方块/格）。
        // 想扩大范围就调大这个数字（例如 2.0 = 半径两格），想缩小就调小。
        double x = player.getX() + (RANDOM.nextDouble() - 0.5) * 1;
        double y = player.getY() + player.getHeight() * 0.5 + 0.15 + (RANDOM.nextDouble() - 0.5) * 1;
        double z = player.getZ() + (RANDOM.nextDouble() - 0.5) * 1;

        // ===== 粒子数量与速度 =====
        // spawnParticles 的参数：
        //   - 第 1 个参数：一次生成的粒子数量
        //   - 后面 5 个参数：x, y, z 方向速度与扩散
        // 目前每次只生成 1 个粒子，速度为 0。
        // 想生成更多粒子，把第一个参数改为更大值（如 3）。
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    new ConjureParticleOptions(color),
                    x, y, z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                new Identifier(SSCHexAddon.MOD_ID, "hex_fox_particle"),
                new SerializableData(),
                data -> (type, entity) -> new HexFoxParticlePower(type, entity)
        ).allowCondition();
    }
}