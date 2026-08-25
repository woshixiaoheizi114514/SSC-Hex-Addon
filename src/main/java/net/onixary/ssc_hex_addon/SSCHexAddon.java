package net.onixary.ssc_hex_addon;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.mana.ManaRegistries;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_FamiliarFox2;
import net.onixary.shapeShifterCurseFabric.player_form.utils.InstinctUtils;


//import net.onixary.ssc_hex_addon.client.HexFoxParticleHandler;
//原粒子效果调用，会导致服务端无法正常启动

import net.onixary.ssc_hex_addon.hex.*;


import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import static net.onixary.shapeShifterCurseFabric.player_form.NormalForm.NORMAL_SCALE_FUNC_BUILDER;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_FamiliarFox3;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.NormalGroup;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;


import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;

import net.minecraft.util.Pair;

import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;


import net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatus;
import net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusPotion;
import net.minecraft.potion.Potion;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.onixary.shapeShifterCurseFabric.items.RegCustomPotions;
import net.minecraft.registry.Registries;
import net.minecraft.entity.effect.StatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;


import net.minecraft.item.Item;
import net.onixary.ssc_hex_addon.item.HexFoxNecklace;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import net.onixary.ssc_hex_addon.power.HexFoxParticlePower;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import io.github.apace100.apoli.registry.ApoliRegistries;

import net.onixary.ssc_hex_addon.power.HexFoxAmethystPower;
import io.github.apace100.apoli.registry.ApoliRegistries;



/*import io.github.apace100.apoli.registry.ApoliRegistries;*/


public class SSCHexAddon implements ModInitializer {
    public static final String MOD_ID = "ssc-hexaddon";


    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }


    // 常量定义（放在类顶部）
    public static final Identifier HEX_FOX_MEDIA_0 = new Identifier(MOD_ID, "hex_fox_media_0");
    public static final Identifier HEX_FOX_MEDIA_1 = new Identifier(MOD_ID, "hex_fox_media_1");
    public static final Identifier HEX_FOX_MEDIA_2 = new Identifier(MOD_ID, "hex_fox_media_2");
    public static final Identifier HEX_FOX_MEDIA_3 = new Identifier(MOD_ID, "hex_fox_media_3");

    // 用于环境扩展的 Key，每个扩展实例需要一个唯一 Key
    private static final CastingEnvironmentComponent.Key<ExtractMediaPreImpl> MEDIA_PRE_KEY = new CastingEnvironmentComponent.Key<>() {};


    // 创造模式物品栏
    public static final ItemGroup SSC_HEX_ADDON_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.ssc-hexaddon.main"))
            .icon(() -> new ItemStack(Registries.ITEM.get(new Identifier(MOD_ID, "hex_fox_necklace")))) // 图标
            .build();


    @Override
    public void onInitialize() {
        Registry<ActionRegistryEntry> registry = IXplatAbstractions.INSTANCE.getActionRegistry();

        // 探知幻形
        Registry.register(registry,
                new Identifier(MOD_ID, "get_ssc_form"),
                new ActionRegistryEntry(HexPattern.fromAngles("aqa", HexDir.SOUTH_EAST), OpGetSSCForm.INSTANCE)
        );

        // 定时形态设定
        Registry.register(registry,
                new Identifier(MOD_ID, "set_ssc_form"),
                new ActionRegistryEntry(HexPattern.fromAngles("wqwqwqwaqeeeq", HexDir.WEST), OpSetSSCForm.INSTANCE)
        );


        // 创建 RegistryKey
        RegistryKey<ItemGroup> groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(MOD_ID, "main"));
// 注册物品组
        Registry.register(Registries.ITEM_GROUP, groupKey, SSC_HEX_ADDON_GROUP);
// 向物品组添加物品
        ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
            entries.add(Registries.ITEM.get(new Identifier(MOD_ID, "hex_fox_necklace")));
            // 以后可继续添加其他物品：
            // entries.add(Registries.ITEM.get(new Identifier(MOD_ID, "another_item")));
        });





        // 阶段0（初生，直立）
        NormalForm hexFox0 = new NormalForm(new Identifier(MOD_ID, "hex_fox_0"));
        hexFox0.formFlag(FormUtils.StarterForm);
        hexFox0.applyScaleFunc(NORMAL_SCALE_FUNC_BUILDER.apply(0.8f, 1.0f));
        RegPlayerForms.registerPlayerForm(hexFox0);

// 阶段1（成长，直立）
        NormalForm hexFox1 = new NormalForm(new Identifier(MOD_ID, "hex_fox_1"));
        hexFox1.formFlag(FormUtils.InhibitorResist);
        hexFox1.applyScaleFunc(NORMAL_SCALE_FUNC_BUILDER.apply(0.65f, 1.0f));
        RegPlayerForms.registerPlayerForm(hexFox1);

// 阶段2（成熟，直立，有特殊的潜行和骑乘动画）
        NormalForm hexFox2 = new Form_FamiliarFox2(new Identifier(MOD_ID, "hex_fox_2"));
        hexFox2.formFlag(FormUtils.InhibitorResist, FormUtils.LockInstinct, FormUtils.CursedMoonFinalForm, FormUtils.CatalystResist, FormUtils.CanTFToFinalForm);
        hexFox2.applyScaleFunc(NORMAL_SCALE_FUNC_BUILDER.apply(0.55f, 1.0f));
        RegPlayerForms.registerPlayerForm(hexFox2);

// 阶段3（最终形态，四足）
        NormalForm hexFox3 = new Form_FamiliarFox3(new Identifier(MOD_ID, "hex_fox_3"));
        hexFox3.formFlag(FormUtils.NoInstinct, FormUtils.NoCursedMoonEffect, FormUtils.InhibitorImmune, FormUtils.FinalForm);
        hexFox3.bodyType(PlayerFormBodyType.FERAL);
        hexFox3.applyScaleFunc(NORMAL_SCALE_FUNC_BUILDER.apply(0.5f, 0.6f));
        RegPlayerForms.registerPlayerForm(hexFox3);

// 形态组
        RegPlayerForms.registerPlayerFormGroup(
                new NormalGroup(new Identifier(MOD_ID, "group_hex_fox"))
                        .registerForm(1, 1, hexFox0)   // Tier 1
                        .registerForm(2, 1, hexFox1)   // Tier 2
                        .registerForm(3, 1, hexFox2)   // Tier 3
                        .registerForm(4, 1, hexFox3)   // Tier 4
        );


        //HexFoxParticleHandler.register();
        //原粒子效果调用，会导致服务端无法正常启动

        /*Registry.register(ApoliRegistries.POWER_FACTORY, id("hex_fox_particle"), HexFoxParticlePower.getFactory());*/


        // -- hex_fox_media_0 --
        ManaRegistries.registerManaType(
                HEX_FOX_MEDIA_0,
                // 最大媒介值（请根据平衡性调整 base 值）
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_media_0"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 最大容量 */ 250000d, 1.0d, 0d)
                                )
                        )
                ),
                // 恢复速度（每秒）
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_regen_0"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 每秒恢复 */ 0d, 1.0d, 0d)
                                )
                        )
                ),
                ManaRegistries.EMPTY_MANA_HANDLER
        );
        // -- hex_fox_media_1 --
        ManaRegistries.registerManaType(
                HEX_FOX_MEDIA_1,
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_media_1"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 最大容量 */ 500000d, 1.0d, 0d)
                                )
                        )
                ),

                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_regen_1"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 每秒恢复 */ 0d, 1.0d, 0d)
                                )
                        )
                ),

                ManaRegistries.EMPTY_MANA_HANDLER
        );

// -- hex_fox_media_2 --
        ManaRegistries.registerManaType(
                HEX_FOX_MEDIA_2,
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_media_2"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 最大容量 */ 1000000d, 1.0d, 0d)
                                )
                        )
                ),
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_regen_2"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(/* 每秒恢复 */ 100d, 1.0d, 0d)
                                )
                        )
                ),
                ManaRegistries.EMPTY_MANA_HANDLER
        );
        // 在 onInitialize 中添加
        ManaRegistries.registerManaType(
                HEX_FOX_MEDIA_3,
                // 最大媒介值（初始 100，可根据需要调整）
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_media_3"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(4000000d, 1.0d, 0d)
                                )
                        )
                ),
                // 恢复速度（每秒 1 点，后续可改）
                new ManaUtils.ModifierList(
                        new Pair<>(new Identifier(MOD_ID, "base_regen_3"),
                                new Pair<>(ManaRegistries.MC_AlwaysTrue,
                                        new ManaUtils.Modifier(1000d, 1.0d, 0d)
                                )
                        )
                ),
                ManaRegistries.EMPTY_MANA_HANDLER
        );


        CastingEnvironment.addCreateEventListener((env, userData) -> {
            if (env instanceof PlayerBasedCastEnv playerEnv) {
                env.addExtension(new ExtractMediaPreImpl(playerEnv));
            }
        });



        // 施法本能：每次施法时，若玩家拥有 hex_fox 媒介条，则增加本能值
        CastingEnvironment.addCreateEventListener((env, userData) -> {
            if (env instanceof PlayerBasedCastEnv playerEnv) {
                ServerPlayerEntity player = playerEnv.getCaster();
                if (player == null) return;

                // 检查是否拥有任意 hex_fox 媒介条
                boolean hasHexFoxMedia = false;
                for (Identifier id : new Identifier[]{
                        HEX_FOX_MEDIA_0
                }) {
                    if (ManaUtils.isManaTypeExists(player, id, null)) {
                        hasHexFoxMedia = true;
                        break;
                    }
                }
                if (!hasHexFoxMedia) return;

                // 添加 PostExecution 扩展，每个图案执行后增加本能
                env.addExtension(new CastingEnvironmentComponent.PostExecution() {
                    @Override
                    public void onPostExecution(CastResult result) {
                        InstinctUtils.addInstinctEffect(player,
                                new Identifier(MOD_ID, "hex_cast"),
                                0.05f,   // 每 tick 增加 0.1 点
                                5,     // 持续 10 ticks（0.5 秒）
                                false); // false = 持续效果
                    }

                    @Override
                    public CastingEnvironmentComponent.Key<?> getKey() {
                        return new CastingEnvironmentComponent.Key<>() {};
                    }
                });
            }
        });


        // 1. 注册状态效果
        BaseTransformativeStatusEffect hexFox0Effect = Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(MOD_ID, "hex_fox_0_effect"),
                new TransformativeStatus(hexFox0)
        );

// 2. 注册药水效果（缺失的关键步骤）
        StatusEffect hexFox0PotionEffect = Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(MOD_ID, "hex_fox_0_potion_effect"),
                new TransformativeStatusPotion(hexFox0Effect)
        );

// 3. 注册药水（使用统一的变量名）
        Potion hexFox0FormPotion = Registry.register(
                Registries.POTION,
                new Identifier(MOD_ID, "hex_fox_0_form_potion"),
                new Potion(new StatusEffectInstance(hexFox0PotionEffect, 1))
        );

// 4. 酿造配方（使用正确的变量名）
        BrewingRecipeRegistry.registerPotionRecipe(
                RegCustomPotions.FAMILIAR_FOX_FORM_POTION,
                HexItems.CHARGED_AMETHYST,
                hexFox0FormPotion   // 改为小写开头的变量名
        );



        // 注册状态效果
        StatusEffect hexFoxRestorative = Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(MOD_ID, "hex_fox_restorative"),
                new HexFoxRestorativeEffect()
        );

// 注册药水（持续 2 tick，不显示粒子/图标，即用即消失）
        Potion hexFoxRestorativePotion = Registry.register(
                Registries.POTION,
                new Identifier(MOD_ID, "hex_fox_restorative_potion"),
                new Potion(new StatusEffectInstance(hexFoxRestorative, 2, 0, false, true, true))
        );

// 酿造配方：水瓶 + 紫水晶粉
        BrewingRecipeRegistry.registerPotionRecipe(
                Potions.WATER,
                HexItems.AMETHYST_DUST,   // 紫水晶粉
                hexFoxRestorativePotion
        );

        //hex_fox粒子效果
        //HexFoxParticleServerHandler.register();
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                id("hex_fox_particle"),
                HexFoxParticlePower.getFactory()
        );


        //紫水晶母岩上煤质恢复加速power
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                id("hex_fox_amethyst_boost"),
                HexFoxAmethystPower.getFactory()
        );




        //注册物品
        Registry.register(
                Registries.ITEM,
                new Identifier(MOD_ID, "hex_fox_necklace"),
                new HexFoxNecklace(new Item.Settings().maxCount(1))
        );


        // 修复线程安全和线程泄漏问题
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            OpSetSSCForm.shutdownScheduler();
        });


        //hex_fox粒子效果power
        //AdditionalPowers.register(HexFoxParticlePower.getFactory());






    }
}