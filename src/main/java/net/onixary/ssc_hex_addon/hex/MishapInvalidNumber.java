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

public class MishapInvalidNumber extends Mishap {
    @Override
    public @NotNull FrozenPigment accentColor(@NotNull CastingEnvironment ctx, @NotNull Context errorCtx) {
        return new FrozenPigment(new ItemStack(Items.RED_DYE), Util.NIL_UUID);
    }

    @Override
    public void execute(@NotNull CastingEnvironment env, @NotNull Context errorCtx, @NotNull List<Iota> stack) {
        // 消耗栈顶三个参数，压入三个垃圾
        int count = Math.min(stack.size(), 3);
        for (int i = 0; i < count; i++) {
            stack.remove(stack.size() - 1);
        }
        for (int i = 0; i < count; i++) {
            stack.add(new GarbageIota());
        }
    }

    @Nullable
    @Override
    protected Text errorMessage(@NotNull CastingEnvironment ctx, @NotNull Context errorCtx) {
        return Text.translatable("hexcasting.mishap.ssc-hexaddon.invalid_number");
    }
}