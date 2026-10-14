package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.client.CeobeSpearRenderer;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.custom.ThrownCeobeSpear;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

// Ceobe's Spear. Stats copied from the vanilla trident (250 durability, 9 attack damage, 1.1 attack speed,
// 1 durability per hit, 2 per block broken). Right click: grants "很重的枪" / Really Heavy Spear (ceobe_power)
// for 60 s, then an 80 s cooldown that starts once the effect ends (TimedActivation, same as the other skill
// items). While the effect is active: ATK +210% (the effect's modifier), every hit silences for 5 s, and
// right click instead charges a trident-style throw (ThrownCeobeSpear).
public class CeobeSpearItem extends Item implements CrosshairCounterItem, ActivatableSkillItem, GeoItem {
    private static final String SKILL_LAST_USED_KEY = "yqils_arknights:ceobe_spear_last_used";
    public static final int SKILL_DURATION_TICKS = 60 * 20;
    public static final int SKILL_COOLDOWN_SECONDS = 80;
    public static final int SKILL_COOLDOWN_TICKS = SKILL_COOLDOWN_SECONDS * 20;
    public static final int SILENCE_DURATION_TICKS = 5 * 20;
    private static final int MIN_THROW_CHARGE_TICKS = 14; // 0.7 s per throw
    // Pause after each throw before the next charge starts. Implemented by restarting the hold this many
    // ticks "early", so the elapsed charge is negative during the pause.
    public static final int THROW_PAUSE_TICKS = 4; // 0.2 s
    private static final float THROW_PITCH_UP_DEGREES = 5.0F; // aim a little above the crosshair
    private static final double THROW_HEIGHT_OFFSET = 0.1D; // spawn a little above the default (eye - 0.1)

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    public CeobeSpearItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).durability(250));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    // --- trident-like weapon behaviour ---
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.hasEffect(ModEffects.CEOBE_POWER.get())) {
            target.addEffect(new MobEffectInstance(ModEffects.SILENCE.get(), SILENCE_DURATION_TICKS, 0));
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSound.CEOBE_SPEAR_POWER_HIT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.yqils_arknights.ceobe_spear.description"));
        super.appendHoverText(stack, level, components, flag);
    }

    // --- skill: right click ---
    // While the skill is active, right click charges a throw instead (the client knows its own effects,
    // so both sides agree on which branch to take).
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.hasEffect(ModEffects.CEOBE_POWER.get())) {
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                return InteractionResultHolder.fail(stack); // same as the trident: don't throw a breaking spear
            }
            player.startUsingItem(hand); // charge sound plays from tickThrowCharge when the charge starts
            return InteractionResultHolder.consume(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        return activateSkill(stack, level, player)
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    // Auto-throw: called every use tick (both sides) from ModEventHandlers#onCeobeSpearChargeTick via
    // LivingEntityUseItemEvent.Tick, with the ticks remaining on the current hold. Once a charge is full
    // (1 s) the server throws a projectile, and both sides restart the charge by
    // returning a fresh duration -- so holding right click keeps throwing. Letting go early does nothing.
    public int tickThrowCharge(ItemStack stack, Player player, int remaining) {
        int elapsed = this.getUseDuration(stack) - remaining;
        Level level = player.level();
        if (elapsed < 0) return remaining; // pause after a throw -- nothing charges yet
        if (elapsed == 0 && !level.isClientSide && player.hasEffect(ModEffects.CEOBE_POWER.get())) {
            // A charge is starting (first press, or the pause after a throw just ended).
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSound.CEOBE_SPEAR_POWER_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (elapsed < MIN_THROW_CHARGE_TICKS) return remaining;
        if (!player.hasEffect(ModEffects.CEOBE_POWER.get())) return remaining; // skill ended mid-hold: just hold
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) return remaining; // don't break the spear

        if (!level.isClientSide) {
            throwSpear(stack, level, player);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return this.getUseDuration(stack) + THROW_PAUSE_TICKS;
    }

    // Spawns a separate projectile carrying the thrower's current ATTACK_DAMAGE (so the skill's +210%
    // applies); the spear itself stays in hand and loses 1 durability per throw.
    private void throwSpear(ItemStack stack, Level level, Player player) {
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ThrownCeobeSpear spear = new ThrownCeobeSpear(level, player, stack, damage);
        spear.setPos(spear.getX(), spear.getY() + THROW_HEIGHT_OFFSET, spear.getZ());
        spear.shootFromRotation(player, player.getXRot() - THROW_PITCH_UP_DEGREES, player.getYRot(), 0.0F, 2.5F, 1.0F);
        spear.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(spear);
        // No custom throw sound -- vanilla trident throw.
        level.playSound(null, spear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public RegistryObject<MobEffect> getSkillEffect() {
        return ModEffects.CEOBE_POWER;
    }

    @Override
    public int getSkillDurationTicks() {
        return SKILL_DURATION_TICKS;
    }

    @Override
    public int getSkillCooldownTicks() {
        return SKILL_COOLDOWN_TICKS;
    }

    @Override
    public String getSkillLastUsedKey() {
        return SKILL_LAST_USED_KEY;
    }

    // No skill sound yet (the interface default would play Mlynar's).
    @Override
    public RegistryObject<SoundEvent> getSkillSound() {
        return null;
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return TimedActivation.getElapsedSeconds(stack, level, SKILL_LAST_USED_KEY, SKILL_DURATION_TICKS, SKILL_COOLDOWN_SECONDS)
                + "/" + SKILL_COOLDOWN_SECONDS;
    }

    // --- GeckoLib ---
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ClientHandler());
    }

    private static final class ClientHandler implements IClientItemExtensions {
        private CeobeSpearRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
                this.renderer = new CeobeSpearRenderer();
            }
            return this.renderer;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(IDLE_ANIM)));
    }

    // Same reason as TyphonGreatbowItem: one controller per display context.
    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }
}
