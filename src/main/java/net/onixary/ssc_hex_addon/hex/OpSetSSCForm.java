package net.onixary.ssc_hex_addon.hex;

import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.IFormGroup;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.InstinctUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OpSetSSCForm implements SpellAction {
    public static final OpSetSSCForm INSTANCE = new OpSetSSCForm();

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    // 可用的目标形态列表（顺序即法术编号顺序）
    private static final List<Identifier> ALLOWED_FORMS = List.of(
            Identifier.of("shape-shifter-curse", "bat_0"),
            Identifier.of("shape-shifter-curse", "bat_1"),
            Identifier.of("shape-shifter-curse", "bat_2"),
            Identifier.of("shape-shifter-curse", "axolotl_0"),
            Identifier.of("shape-shifter-curse", "axolotl_1"),
            Identifier.of("shape-shifter-curse", "axolotl_2"),
            Identifier.of("shape-shifter-curse", "ocelot_0"),
            Identifier.of("shape-shifter-curse", "ocelot_1"),
            Identifier.of("shape-shifter-curse", "ocelot_2"),
            Identifier.of("shape-shifter-curse", "familiar_fox_0"),
            Identifier.of("shape-shifter-curse", "familiar_fox_1"),
            Identifier.of("shape-shifter-curse", "familiar_fox_2"),
            Identifier.of("shape-shifter-curse", "snow_fox_0"),
            Identifier.of("shape-shifter-curse", "snow_fox_1"),
            Identifier.of("shape-shifter-curse", "snow_fox_2"),
            Identifier.of("shape-shifter-curse", "anubis_wolf_0"),
            Identifier.of("shape-shifter-curse", "anubis_wolf_1"),
            Identifier.of("shape-shifter-curse", "anubis_wolf_2"),
            Identifier.of("shape-shifter-curse", "spider_0"),
            Identifier.of("shape-shifter-curse", "spider_1"),
            Identifier.of("shape-shifter-curse", "spider_2")
    );

    private static final Identifier ORIGINAL_BEFORE_ENABLE = Identifier.of("shape-shifter-curse", "original_before_enable");

    // 记录正在进行法术效果的玩家（防重复施法），使用并发集合确保线程安全
    private static final Set<UUID> activeTransforms = ConcurrentHashMap.newKeySet();

    /**
     * 获取形态的阶段（tier）。通过遍历所有形态组找到该形态所属的 tier。
     * @param form 要查询的形态
     * @return tier 值，如果未找到则返回 -1
     */
    private static int getTier(IForm form) {
        for (IFormGroup group : RegPlayerForms.playerFormGroups.values()) {
            for (Map.Entry<Integer, List<Pair<Integer, IForm>>> entry : group.getGroupData().entrySet()) {
                for (Pair<Integer, IForm> pair : entry.getValue()) {
                    if (pair.getRight().isEquals(form)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 在模组卸载或服务器停止时调用，关闭调度器释放线程资源。
     */
    public static void shutdownScheduler() {
        SCHEDULER.shutdownNow();
    }

    @Override
    public int getArgc() {
        return 3;
    }

    @Override
    public boolean hasCastingSound(CastingEnvironment ctx) {
        return true;
    }

    @Override
    public boolean awardsCastingStat(CastingEnvironment ctx) {
        return true;
    }

    @Override
    public Result execute(List<? extends Iota> args, CastingEnvironment env) {
        if (!(args.get(0) instanceof EntityIota entityIota)) {
            throw new MishapNotPlayer();
        }
        Entity entity = entityIota.getEntity();
        if (!(entity instanceof ServerPlayerEntity player)) {
            throw new MishapNotPlayer();
        }

        if (!(args.get(1) instanceof DoubleIota indexIota) || !(args.get(2) instanceof DoubleIota durationIota)) {
            throw new MishapInvalidNumber();
        }

        int index = (int) indexIota.getDouble();
        double duration = durationIota.getDouble();

        int maxIndex = ALLOWED_FORMS.size();
        if (index < 1 || index > maxIndex) {
            throw new MishapInvalidNumber();
        }

        Identifier targetFormId = ALLOWED_FORMS.get(index - 1);
        IForm targetForm = RegPlayerForms.getPlayerForm(targetFormId);
        if (targetForm == null) {
            throw new MishapInvalidNumber();
        }

        PlayerFormComponent comp = PlayerFormComponent.COMPONENT.get(player);
        IForm currentForm = comp.nowForm;
        Identifier currentFormId = currentForm == null ? null : currentForm.getFormID();

        // 1. 原始未启用形态 -> 占位文本1
        if (currentFormId != null && currentFormId.equals(ORIGINAL_BEFORE_ENABLE)) {
            return messageResult("法术好像没有起效果？目标好像不受月之魔力的影响");
        }

        int currentTier = currentForm == null ? -1 : getTier(currentForm);

        // 2. 阶段3（最终形态） -> 占位文本2
        if (currentTier == 4) {
            return messageResult("法术好像没有起效果？目标好像受到月之魔力的影响太深让这些月之魔力不足以在对其产生影响");
        }

        // 3. 允许变形的阶段（0,1,2） -> 正常变形
        if (currentTier == 0 || currentTier == 1 || currentTier == 2 || currentTier == 3) {
            // 检查目标是否已在法术持续时间中
            if (activeTransforms.contains(player.getUuid())) {
                return messageResult("法术失败了？好像目标体内有一股力量在排斥这些新的月之魔力");
            }

            // 记录原始形态（final 以确保 lambda 可捕获）
            IForm tempOriginal = comp.nowForm;
            if (tempOriginal == null) {
                tempOriginal = RegPlayerForms.ORIGINAL_SHIFTER;
            }
            final IForm originalForm = tempOriginal;

            // 锁定本能，加入活跃集合
            InstinctUtils.playerInstinctLock.put(player.getUuid(), true);
            activeTransforms.add(player.getUuid());

            // 立即变形
            TransformManager.immediatelyTransform(player, targetForm);

            long delayTicks = (long) (duration * 20);
            long cost = (long) (duration * 20000);

            RenderedSpell spell = new RenderedSpell() {
                @Override
                public void cast(CastingEnvironment env) {
                    SCHEDULER.schedule(() -> {
                        env.getWorld().getServer().execute(() -> {
                            ServerPlayerEntity currentPlayer = player.server.getPlayerManager().getPlayer(player.getUuid());
                            if (currentPlayer == null) {
                                // 玩家离线也要清理状态
                                activeTransforms.remove(player.getUuid());
                                InstinctUtils.playerInstinctLock.put(player.getUuid(), false);
                                return;
                            }

                            PlayerFormComponent compNow = PlayerFormComponent.COMPONENT.get(currentPlayer);
                            IForm currentFormNow = compNow.nowForm;
                            if (currentFormNow != null && currentFormNow.isEquals(targetForm)) {
                                TransformManager.immediatelyTransform(currentPlayer, originalForm);
                            }

                            // 还原后解锁本能并移除活跃标记
                            InstinctUtils.playerInstinctLock.put(player.getUuid(), false);
                            activeTransforms.remove(player.getUuid());
                        });
                    }, delayTicks * 50, TimeUnit.MILLISECONDS);
                }

                @Override
                public CastingImage cast(CastingEnvironment env, CastingImage image) {
                    cast(env);
                    return null;
                }
            };

            return new Result(spell, cost, Collections.emptyList(), 0L);
        }

        // 4. 其他任何未知阶段 -> 占位文本3（安全回退）
        return messageResult("法术好像没有起效果？目标的形态好像有些\"特别\"让这些月之魔力无法对其产生影响");
    }

    /**
     * 创建一个只显示消息的 Result，不消耗媒质，不改变栈。
     */
    private Result messageResult(String message) {
        RenderedSpell msgSpell = new RenderedSpell() {
            @Override
            public void cast(CastingEnvironment env) {
                if (env.getCaster() != null) {
                    env.getCaster().sendMessage(Text.literal(message), false);
                }
            }

            @Override
            public CastingImage cast(CastingEnvironment env, CastingImage image) {
                cast(env);
                return null;
            }
        };
        return new Result(msgSpell, 0, Collections.emptyList(), 0L);
    }

    @Override
    public Result executeWithUserdata(List<? extends Iota> args, CastingEnvironment env, NbtCompound userData) {
        return execute(args, env);
    }

    @Override
    public OperationResult operate(CastingEnvironment env, CastingImage image, SpellContinuation continuation) {
        return SpellAction.DefaultImpls.operate(this, env, image, continuation);
    }
}