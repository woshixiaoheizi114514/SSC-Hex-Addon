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
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpGetSSCForm implements SpellAction {
    public static final OpGetSSCForm INSTANCE = new OpGetSSCForm();

    @Override
    public int getArgc() {
        return 1;
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
        Iota arg = args.get(0);
        if (!(arg instanceof EntityIota entityIota)) {
            throw new MishapNotPlayer();
        }
        Entity entity = entityIota.getEntity();
        if (!(entity instanceof ServerPlayerEntity player)) {
            throw new MishapNotPlayer();
        }

        PlayerFormComponent comp = PlayerFormComponent.COMPONENT.get(player);
        IForm currentForm = comp.nowForm;
        int tier;
        if (currentForm == null || currentForm.getFormID().equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID())) {
            tier = -1;
        } else {
            tier = currentForm.getFormTier();
        }

        RenderedSpell spell = new RenderedSpell() {
            @Override
            public void cast(CastingEnvironment env) {
                // 空实现
            }

            @Override
            public CastingImage cast(CastingEnvironment env, CastingImage image) {
                List<Iota> newStack = new ArrayList<>(image.getStack());
                newStack.add(new DoubleIota(tier));
                // 复制 CastingImage，只修改栈
                return image.copy(
                        newStack,
                        image.getParenCount(),
                        image.getParenthesized(),
                        image.getEscapeNext(),
                        image.getOpsConsumed(),
                        image.getUserData()
                );
            }
        };

        return new Result(spell, 0, Collections.emptyList(), 0L);
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