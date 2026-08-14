package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.effect.custom.ImmolationEffect;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class IfritFlamethrowerItem extends Item {
    private static final double CONE_HALF_ANGLE_DEGREES = 30.0;
    private static final int DAMAGE_INTERVAL_TICKS = 10; // apply damage 2x/sec
    private static final int PARTICLE_STEPS = 6; // clusters spaced along the stream, from face to max range
    private static final int PARTICLES_PER_STEP = 2;
    private static final int PARTICLE_VISIBLE_TICKS = 5; // how long the stream shows per attack

    // Skill effect: the next damage application after activation deals 250% damage and applies
    // Immolation (3s) to everything it hits, then reverts to normal until the skill fires again.
    private static final float BOOSTED_DAMAGE_MULTIPLIER = 2.5F;
    private static final int IMMOLATION_DURATION_TICKS = 60; // 3s
    private static final float IMMOLATION_DAMAGE_PERCENT = 0.33F; // of the attacker's ATTACK_DAMAGE at apply time
    private static final String PENDING_BOOST_KEY = "yqils_arknights:flamethrower_pending_boost";


    public static final int MAX_CHARGES = 3;
    public static final int CHARGE_COOLDOWN_TICKS = 140; // 7s per charge
    public static final int CHARGE_COOLDOWN_SECONDS = CHARGE_COOLDOWN_TICKS / 20;
    private static final String CHARGES_KEY = "yqils_arknights:flamethrower_charges";
    private static final String LAST_REGEN_TICK_KEY = "yqils_arknights:flamethrower_last_regen";

    // Durability doubles as "fuel": drains 1/sec while firing, blocks use once it's down to 1 so it
    // can never actually break through this drain, and recovers 1/sec while idle (2/sec once above
    // half of max durability).
    private static final int DURABILITY_DRAIN_INTERVAL_TICKS = 20; // 1s
    private static final int DURABILITY_RECOVER_INTERVAL_TICKS = 20; // 1s
    private static final int DURABILITY_FAST_RECOVER_THRESHOLD = 50;
    private static final int MIN_DURABILITY_TO_USE = 1;
    private static final int EMPTY_COOLDOWN_TICKS = 200; // 10s, applied when forced to stop from low durability
    private static final String LAST_DURABILITY_RECOVER_TICK_KEY = "yqils_arknights:flamethrower_last_durability_recover";

    private static int getRemainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        components.add(Component.translatable("item.yqils_arknights.ifrit_flamethrower.description"));
        super.appendHoverText(p_41421_, p_41422_, components, p_41424_);
    }

    private static void tickDurabilityRecovery(ItemStack stack, Level level, Entity entity) {
        boolean isBeingUsed = entity instanceof LivingEntity livingEntity
                && livingEntity.isUsingItem()
                && livingEntity.getUseItem() == stack;
        if (isBeingUsed) {
            return;
        }

        int remaining = getRemainingDurability(stack);
        if (remaining >= stack.getMaxDamage()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        long lastRecover = tag.getLong(LAST_DURABILITY_RECOVER_TICK_KEY);
        long elapsed = level.getGameTime() - lastRecover;
        if (elapsed >= DURABILITY_RECOVER_INTERVAL_TICKS) {
            int amount = remaining > DURABILITY_FAST_RECOVER_THRESHOLD ? 2 : 1;
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
            tag.putLong(LAST_DURABILITY_RECOVER_TICK_KEY, level.getGameTime());
        }
    }

    public static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CHARGES_KEY)) return MAX_CHARGES;
        return tag.getInt(CHARGES_KEY);
    }

    // Seconds elapsed toward the *next* charge -- capped/shown as full once at MAX_CHARGES,
    // since there's nothing left to regenerate toward.
    public static int getRegenElapsedSeconds(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return CHARGE_COOLDOWN_SECONDS;
        int charges = tag.contains(CHARGES_KEY) ? tag.getInt(CHARGES_KEY) : MAX_CHARGES;
        if (charges >= MAX_CHARGES) return CHARGE_COOLDOWN_SECONDS;
        long elapsedTicks = level.getGameTime() - tag.getLong(LAST_REGEN_TICK_KEY);
        long elapsedSeconds = elapsedTicks / 20;
        return (int) Math.min(elapsedSeconds, CHARGE_COOLDOWN_SECONDS);
    }

    private static void tickChargeRegen(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        int charges = tag.contains(CHARGES_KEY) ? tag.getInt(CHARGES_KEY) : MAX_CHARGES;

        if (charges >= MAX_CHARGES) {
            tag.putInt(CHARGES_KEY, MAX_CHARGES);
            tag.putLong(LAST_REGEN_TICK_KEY, level.getGameTime());
            return;
        }

        long lastRegen = tag.getLong(LAST_REGEN_TICK_KEY);
        long elapsed = level.getGameTime() - lastRegen;
        if (elapsed >= CHARGE_COOLDOWN_TICKS) {
            int gained = (int) (elapsed / CHARGE_COOLDOWN_TICKS);
            tag.putInt(CHARGES_KEY, Math.min(MAX_CHARGES, charges + gained));
            tag.putLong(LAST_REGEN_TICK_KEY, lastRegen + (long) gained * CHARGE_COOLDOWN_TICKS);
        }
    }

    // Flags the next damage application (in onUseTick) to be boosted -- actually applying the
    // damage/effect happens there, since that's where the target list is already being built.
    private static void activateSkill(Level level, LivingEntity entity, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(PENDING_BOOST_KEY, true);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                ModSound.IFRIT_FLAMETHROWER_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide) {
            tickChargeRegen(stack, level);
            tickDurabilityRecovery(stack, level, entity);
        }
    }

    // Same base angles as vanilla's CROSSBOW_HOLD (AnimationUtils.animateCrossbowHold), just without
    // the "+ head.yRot" / "+ head.xRot" terms that make that pose track the player's view.
    private static final HumanoidModel.ArmPose FLAMETHROWER_HOLD = HumanoidModel.ArmPose.create(
            "yqils_arknights:flamethrower_hold", true,
            (model, entity, arm) -> {
                boolean mainHand = arm == entity.getMainArm();
                ModelPart primary = mainHand ? model.rightArm : model.leftArm;
                ModelPart offhand = mainHand ? model.leftArm : model.rightArm;
                primary.yRot = mainHand ? -0.3F : 0.3F;
                offhand.yRot = mainHand ? 0.6F : -0.6F;
                primary.xRot = -0.5F;
                offhand.xRot = -0.5F;
            });

    // Hidden (no "+4 Attack Damage" tooltip line -- see getDefaultTooltipHideFlags) flat bonus,
    // since the item's own custom description already covers its stats.
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("cb9093a4-a1ac-4696-99cb-0a31b7968359");
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public IfritFlamethrowerItem(Properties p_41383_) {
        super(p_41383_.rarity(Rarity.EPIC).stacksTo(1).durability(100));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 4.0D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return ItemStack.TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1900;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if (getRemainingDurability(stack) <= MIN_DURABILITY_TO_USE) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(stack);
    }

    // Called every tick while held-down use continues -- this is where the actual flamethrower
    // damage happens. Cone range matches the entity's own current reach attribute, so effects
    // like Unbrilliant Glory's reach boost extend this too.
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            return;
        }

        if (remainingUseDuration % DURABILITY_DRAIN_INTERVAL_TICKS == 0) {
            if (getRemainingDurability(stack) <= MIN_DURABILITY_TO_USE) {
                entity.stopUsingItem();
                if (entity instanceof Player player && !player.getCooldowns().isOnCooldown(this)) {
                    player.getCooldowns().addCooldown(this, EMPTY_COOLDOWN_TICKS);
                }
                return;
            }
            stack.setDamageValue(stack.getDamageValue() + 1);
        }

        if (remainingUseDuration % DAMAGE_INTERVAL_TICKS == 0) {
            tryActivateSkill(level, entity, stack);
        }

        double range = entity.getAttributeValue(ForgeMod.ENTITY_REACH.get())+2;
        Vec3 lookVec = entity.getLookAngle();

        // Particles stay visible for PARTICLE_VISIBLE_TICKS starting at each real attack tick,
        // instead of a single-tick pop or spawning every tick regardless of whether an attack happened.
        int ticksIntoInterval = (int) (remainingUseDuration % DAMAGE_INTERVAL_TICKS);
        int ticksSinceAttack = (DAMAGE_INTERVAL_TICKS - ticksIntoInterval) % DAMAGE_INTERVAL_TICKS;
        if (ticksSinceAttack < PARTICLE_VISIBLE_TICKS) {
            spawnFlameStream(level, entity, lookVec, range);
        }

        if (ticksIntoInterval != 0) {
            return;
        }

        double cosThreshold = Math.cos(Math.toRadians(CONE_HALF_ANGLE_DEGREES));

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(range),
                target -> target != entity && target.isAlive()
        );

        CompoundTag tag = stack.getOrCreateTag();
        boolean boosted = tag.getBoolean(PENDING_BOOST_KEY);
        float baseDamage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = boosted ? baseDamage * BOOSTED_DAMAGE_MULTIPLIER : baseDamage;
        float immolationDamage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE) * IMMOLATION_DAMAGE_PERCENT;

        SoundEvent rangedSound = boosted
                ? ModSound.IFRIT_FLAMETHROWER_POWER_RANGED.get()
                : ModSound.IFRIT_FLAMETHROWER_RANGED.get();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                rangedSound, SoundSource.PLAYERS, 1.0F, 1.0F);

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(entity.position()).normalize();
            if (lookVec.dot(toTarget) >= cosThreshold) {
                target.hurt(entity.damageSources().indirectMagic(entity, entity), damage);
                if (boosted) {
                    target.getPersistentData().putFloat(ImmolationEffect.SOURCE_DAMAGE_KEY, immolationDamage);
                    target.getPersistentData().putUUID(ImmolationEffect.SOURCE_UUID_KEY, entity.getUUID());
                    target.addEffect(new MobEffectInstance(ModEffects.IMMOLATION.get(), IMMOLATION_DURATION_TICKS, 0));
                }
            }
        }

        if (boosted) {
            tag.putBoolean(PENDING_BOOST_KEY, false);
        }
    }

    // Consumes a banked charge and fires the skill. Called on the same DAMAGE_INTERVAL_TICKS cadence
    // as the damage loop, so banked charges drain one per damage tick instead of all at once.
    private static void tryActivateSkill(Level level, LivingEntity entity, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int charges = tag.contains(CHARGES_KEY) ? tag.getInt(CHARGES_KEY) : MAX_CHARGES;
        if (charges <= 0) {
            return;
        }

        tag.putInt(CHARGES_KEY, charges - 1);
        if (charges == MAX_CHARGES) {
            tag.putLong(LAST_REGEN_TICK_KEY, level.getGameTime());
        }

        activateSkill(level, entity, stack);
    }

    // Every tick (not throttled like damage), spawns clusters of flame particles sampled along the
    // look direction out to range, giving a segmented "jet" visual. Broadcast server-side so every
    // nearby player sees it, not just whoever's holding the item.
    private static void spawnFlameStream(Level level, LivingEntity entity, Vec3 lookVec, double range) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Offset below eye level so the stream doesn't render straight down the player's own sightline
        Vec3 origin = entity.getEyePosition().add(0, -0.2, 0);
        for (int i = 1; i <= PARTICLE_STEPS; i++) {
            double distance = range * i / PARTICLE_STEPS;
            Vec3 point = origin.add(lookVec.scale(distance));
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    point.x, point.y, point.z,
                    PARTICLES_PER_STEP, 0.15, 0.15, 0.15, 0.01);
        }
    }

    // PlayerRenderer only consults getUseAnimation() while actively using the item (right-click held);
    // this hook covers the idle-hold case, giving it trident's throw-ready stance at rest too.
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return FLAMETHROWER_HOLD;
            }
        });
    }
}
