package net.onixary.ssc_hex_addon.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import net.onixary.shapeShifterCurseFabric.mana.ManaRegistries;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.ssc_hex_addon.SSCHexAddon;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HexFoxNecklace extends AccessoryItem {

    private static final Identifier MODIFIER_ID = new Identifier(SSCHexAddon.MOD_ID, "hex_fox_necklace");

    public HexFoxNecklace(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.ssc-hexaddon.hex_fox_necklace.tooltip").formatted(Formatting.DARK_PURPLE));
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity entity, SlotData slotData) {
        // 只允许放入项链槽 (Trinkets 的 necklace 槽位于 chest 组)
        if (slotData == null) return false;
        Identifier slotId = slotData.slot();
        return slotId.getPath().equals("chest/necklace");
    }

    @Override
    public void onEquip(ItemStack stack, LivingEntity entity, SlotData slot) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!isHexFox3(player)) return;
        applyModifiers(player);
    }

    @Override
    public void onUnequip(ItemStack stack, LivingEntity entity, SlotData slot) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        removeModifiers(player);
    }

    @Override
    public void accessoryTick(ItemStack stack, LivingEntity entity, SlotData slot) {
        if (entity.getWorld().isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (isHexFox3(player)) {
            applyModifiers(player);
        } else {
            removeModifiers(player);
        }
    }

    private boolean isHexFox3(ServerPlayerEntity player) {
        PlayerFormComponent comp = PlayerFormComponent.COMPONENT.get(player);
        return comp != null && comp.nowFormID != null && comp.nowFormID.equals(SSCHexAddon.id("hex_fox_3"));
    }

    private void applyModifiers(ServerPlayerEntity player) {
        // 最大值减半：第二个参数为乘算系数，0.5 即 50%
        ManaUtils.addMaxManaModifier(player, MODIFIER_ID, ManaRegistries.MC_AlwaysTrue,
                new ManaUtils.Modifier(0d, 0.5d, 0d), false);
        // 恢复速度翻倍：第二个参数为乘算系数，2.0 即 200%（基础 1 + 1 = 2）
        ManaUtils.addRegenManaModifier(player, MODIFIER_ID, ManaRegistries.MC_AlwaysTrue,
                new ManaUtils.Modifier(0d, 2.0d, 0d), false);
    }

    private void removeModifiers(ServerPlayerEntity player) {
        ManaUtils.removeMaxManaModifier(player, MODIFIER_ID, false);
        ManaUtils.removeRegenManaModifier(player, MODIFIER_ID, false);
    }
}