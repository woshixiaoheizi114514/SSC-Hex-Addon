package net.onixary.ssc_hex_addon.hex;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.GarbageIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MishapNotPlayer extends Mishap {

    @Override
    public @NotNull FrozenPigment accentColor(@NotNull CastingEnvironment ctx, @NotNull Context errorCtx) {
        return new FrozenPigment(new ItemStack(Items.RED_DYE), Util.NIL_UUID);
    }

    @Override
    public void execute(@NotNull CastingEnvironment env, @NotNull Context errorCtx, @NotNull List<Iota> stack) {
        // 消耗栈顶的实体 iota（已因事故变得无效），并压入一个垃圾 iota
        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);  // 弹出栈顶元素
        }
        stack.add(createGarbage());          // 压入垃圾
    }

    private Iota createGarbage() {
        return new GarbageIota();
    }

    @Nullable
    @Override
    protected Text errorMessage(@NotNull CastingEnvironment ctx, @NotNull Context errorCtx) {
        return Text.translatable("hexcasting.mishap.ssc-hexaddon.not_player");
    }
}