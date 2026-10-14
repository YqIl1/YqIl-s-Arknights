package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.entity.custom.TyphonLockCircleEntity;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import com.YqIl1.yqils_arknights.client.TyphonGreatbowClient;
import com.YqIl1.yqils_arknights.client.TyphonGreatbowRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraftforge.common.util.TransformationHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

// Typhon's Greatbow (提丰的巨弓). Plain right click draws/fires like a vanilla bow. Shift + right click
// locks onto the highest-health enemy within SCAN_RADIUS; the next LOCKED_SHOTS released arrows are
// each rain ARROWS_PER_SHOT arrows straight down, each onto a random living entity (not the shooter) within SPLASH_RADIUS of the
// lock. Every hit from those arrows stuns for STUN_TICKS (handled in ModEventHandlers via the arrow tag).
// If the locked mob dies (or unloads), the remaining shots keep raining on the last spot it was seen.
public class TyphonGreatbowItem extends BowItem implements CrosshairCounterItem, ActivatableSkillItem, GeoItem {
    private static final String SKILL_LAST_USED_KEY = "yqils_arknights:typhon_skill_last_used";
    private static final String LOCK_TARGET_KEY = "yqils_arknights:typhon_lock_target";
    private static final String LOCK_SHOTS_KEY = "yqils_arknights:typhon_lock_shots";
    private static final String LOCK_X_KEY = "yqils_arknights:typhon_lock_x";
    private static final String LOCK_Y_KEY = "yqils_arknights:typhon_lock_y";
    private static final String LOCK_Z_KEY = "yqils_arknights:typhon_lock_z";
    private static final String LOCK_CIRCLE_KEY = "yqils_arknights:typhon_lock_circle";

    // Persistent-data tag on skill arrows; ModEventHandlers clears hit i-frames and applies the stun for these.
    public static final String SKILL_ARROW_TAG = "yqils_arknights:typhon_skill_arrow";
    public static final int STUN_TICKS = 8; // 0.4s per hit
    // Extra damage each skill arrow adds on top of its own arrow damage, stored on the arrow when it's fired.
    public static final String SKILL_ARROW_BONUS_KEY = "yqils_arknights:typhon_skill_arrow_bonus";
    private static final float SKILL_ARROW_ATTACK_RATIO = 1.75F; // bonus = wielder's ATTACK_DAMAGE * 1.75

    public static final int SKILL_COOLDOWN_SECONDS = 40;
    public static final int SKILL_COOLDOWN_TICKS = SKILL_COOLDOWN_SECONDS * 20;
    // 0 because the cooldown timestamp is only written once the last locked shot is fired (see
    // consumeLockedShot), so recovery starts right then rather than at lock-on.
    private static final int SKILL_DURATION_TICKS = 0;

    private static final double SCAN_RADIUS = 10.0;
    // Every creature from Gathering Torches Become Sunlight can be locked, even if it doesn't count as a vanilla
    // "Enemy". Matched by registry namespace, so there's no compile dependency and nothing breaks without it.
    private static final String TORCHES_BECOME_SUNLIGHT_MODID = "torchesbecomesunlight";
    private static final double SPLASH_RADIUS = 4.0; // must match the lock circle ring (TyphonLockCircleRenderer)
    private static final int LOCKED_SHOTS = 10;
    // Full draw takes 1.5 s while a lock is active (vanilla bows: 20 ticks = 1 s).
    private static final float NORMAL_DRAW_TICKS = 20.0F;
    private static final float LOCKED_DRAW_TICKS = 30.0F;
    private static final int ARROWS_PER_SHOT = 5;
    private static final double DROP_HEIGHT = 6.0;    // blocks above the circle centre when there's no one to aim at
    private static final double HEAD_CLEARANCE = 2.0; // arrows appear this far above the victim's head
    private static final int CIRCLE_CHECK_INTERVAL = 20; // ticks between "is my lock circle still there?" checks
    private static final double DROP_JITTER = 0.25; // small horizontal spread so stacked arrows don't overlap visually
    private static final int LOCK_GLOW_TICKS = 200; // 10s outline on the locked target
    private static final double ATTACK_DAMAGE_BONUS = 7.0D;

    // --- skill particles ---
    // Purple dust burst at each skill arrow's spawn point (same lilac as the lock circle).
    private static final net.minecraft.core.particles.DustParticleOptions SPAWN_BURST =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.70F, 0.48F, 0.88F), 1.0F);
    private static final int STARS_PER_TICK = 2;
    private static final double STAR_FORWARD = 1.1D; // blocks in front of the eyes
    private static final double STAR_SPREAD = 0.3D;
    private static final double STAR_SIDE = 0.2D; // shifted toward the hand holding the bow

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    // --- GeckoLib animation ---
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DRAW_ANIM = RawAnimation.begin().thenPlayAndHold("draw");

    public TyphonGreatbowItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).durability(799));
        // Same setup as EyjafjallaWandItem/IfritFlamethrowerItem: BASE_ATTACK_DAMAGE_UUID makes the tooltip
        // read like a sword's ("8 Attack Damage" = 1 base + 7). Melee is still blocked via the no_melee tag;
        // this mainly feeds the skill arrows' ATTACK_DAMAGE * 1.75 bonus.
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Base attack damage", ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    // Tells Forge to render this item through GeckoLib instead of a normal item model
    // (models/item/typhon_greatbow.json has parent builtin/entity for this). Only called on the client.
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ClientHandler());
    }

    // Nested class so client-only types (the renderer, LocalPlayer, PoseStack) are only resolved when
    // Forge calls initializeClient on the client -- same pattern as IfritFlamethrowerItem.ClientHandler.
    private static final class ClientHandler implements IClientItemExtensions {
        private TyphonGreatbowRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
                this.renderer = new TyphonGreatbowRenderer();
            }
            return this.renderer;
        }

        // First-person pose used while drawing: the display settings the bow had before the idle pose was
        // re-angled (models/item/typhon_greatbow.json firstperson_righthand: no rotation, translation
        // [1, -8, 1.13], scale 0.68). Translation is in 1/16 block units, same as the JSON loader stores it.
        private static final ItemTransform DRAW_POSE = new ItemTransform(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Vector3f(-2.0F / 16.0F, -2.5F / 16.0F, 1.13F / 16.0F),
                new Vector3f(0.68F, 0.68F, 0.68F));

        // While drawing in first person:
        //  1. skip vanilla's bow tilt, but keep its shake and pull-in/stretch (re-applied below);
        //  2. keep the normal held-item position (same numbers as vanilla's applyItemArmTransform);
        //  3. swap the model's current first-person display transform (the new idle angle) for DRAW_POSE.
        //     Vanilla still applies the JSON display transform after this hook, so we apply DRAW_POSE and then
        //     the exact inverse of the current one -- they cancel, leaving DRAW_POSE. Reading the current one from
        //     the baked model means re-tuning the idle angle in Blockbench never changes the draw pose.
        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                               float partialTick, float equipProcess, float swingProcess) {
            InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (!(player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand)) {
                return false;
            }
            int side = arm == HumanoidArm.RIGHT ? 1 : -1;
            poseStack.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);

            // Vanilla's charge feedback (ItemInHandRenderer, UseAnim.BOW case), minus its tilt rotations:
            // a small shake once past 10% draw, plus pulling toward the camera and stretching along the view
            // axis (the "zoom in") as the draw goes from 0 to full. Same formulas and constants as vanilla.
            float drawTicks = (float) itemInHand.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTick + 1.0F);
            float power = drawPower(drawTicks, fullDrawTicks(itemInHand));
            if (power > 0.1F) {
                float shake = Mth.sin((drawTicks - 0.1F) * 1.3F) * (power - 0.1F);
                poseStack.translate(0.0F, shake * 0.004F, 0.0F);
            }
            poseStack.translate(0.0F, 0.0F, power * 0.04F);
            poseStack.scale(1.0F, 1.0F, 1.0F + power * 0.2F);

            boolean leftHand = arm != HumanoidArm.RIGHT;
            ItemDisplayContext context = leftHand ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
            ItemTransform current = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemInHand, player.level(), player, 0).getTransforms().getTransform(context);

            DRAW_POSE.apply(leftHand, poseStack);
            undoTransform(current, leftHand, poseStack);
            return true;
        }

        // Exact inverse of ItemTransform.apply(leftHand, poseStack): the same steps in reverse order, each inverted.
        private static void undoTransform(ItemTransform t, boolean leftHand, PoseStack poseStack) {
            if (t == ItemTransform.NO_TRANSFORM) return;
            float flip = leftHand ? -1.0F : 1.0F;
            poseStack.mulPose(TransformationHelper.quatFromXYZ(
                    t.rightRotation.x(), t.rightRotation.y() * flip, t.rightRotation.z() * flip, true).conjugate());
            poseStack.scale(safeInverse(t.scale.x()), safeInverse(t.scale.y()), safeInverse(t.scale.z()));
            float deg = (float) Math.PI / 180.0F;
            poseStack.mulPose(new Quaternionf().rotationXYZ(
                    t.rotation.x() * deg, t.rotation.y() * flip * deg, t.rotation.z() * flip * deg).conjugate());
            poseStack.translate(-flip * t.translation.x(), -t.translation.y(), -t.translation.z());
        }

        private static float safeInverse(float v) {
            return Math.abs(v) < 1.0E-4F ? 1.0F : 1.0F / v;
        }
    }

    // "draw" plays (and holds on its last frame) while a player is pulling this bow; otherwise "idle" loops.
    // Skipped in the inventory/hotbar GUI, since the hotbar icon is the same stack as the one in hand.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "draw", 2, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            boolean inGui = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI;
            boolean drawing = stack != null && !inGui && TyphonGreatbowClient.isBeingDrawn(stack);
            // "draw" is authored as 1 s; play it at 20/30 speed during the skill so it fills the 1.5 s draw.
            state.setControllerSpeed(stack != null ? NORMAL_DRAW_TICKS / fullDrawTicks(stack) : 1.0F);
            return state.setAndContinue(drawing ? DRAW_ANIM : IDLE_ANIM);
        }));
    }

    // One animation controller per display context (first person, third person, GUI, ...). Without this,
    // the hand render (asking for "draw") and the hotbar icon render (asking for "idle") share a single
    // controller and flip it every frame, so it stays stuck in TRANSITIONING and "draw" never starts.
    // Must stay a constant: it's read while geoCache is being created in the constructor.
    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // While drawing during the skill (a lock is active), purple stars gather in front of the bow.
    // Client-side only; runs for every player the client can see drawing, so others see it too.
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, entity, stack, remainingUseDuration);
        if (!level.isClientSide || !hasLock(stack)) return;
        net.minecraft.world.phys.Vec3 look = entity.getLookAngle();
        // Horizontal "right" of where the entity faces; flipped when the bow is in the left hand.
        double yaw = Math.toRadians(entity.getYRot());
        net.minecraft.world.entity.HumanoidArm arm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? entity.getMainArm() : entity.getMainArm().getOpposite();
        double side = arm == net.minecraft.world.entity.HumanoidArm.RIGHT ? STAR_SIDE : -STAR_SIDE;
        net.minecraft.world.phys.Vec3 front = entity.getEyePosition().add(look.scale(STAR_FORWARD))
                .add(-Math.cos(yaw) * side, -0.15D, -Math.sin(yaw) * side);
        for (int i = 0; i < STARS_PER_TICK; i++) {
            double ox = (level.random.nextDouble() * 2 - 1) * STAR_SPREAD;
            double oy = (level.random.nextDouble() * 2 - 1) * STAR_SPREAD;
            double oz = (level.random.nextDouble() * 2 - 1) * STAR_SPREAD;
            level.addParticle(com.YqIl1.yqils_arknights.particle.ModParticles.TYPHON_STAR.get(),
                    front.x + ox, front.y + oy, front.z + oz,
                    -ox * 0.05D, -oy * 0.05D + 0.005D, -oz * 0.05D);
        }
    }

    // Gives each bow its own GeckoLib id, so two bows being drawn at once don't share one animation.
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
            if (entity instanceof Player player && hasLock(stack) && entity.tickCount % CIRCLE_CHECK_INTERVAL == 0) {
                ensureLockCircle(stack, serverLevel, player);
            }
        }
    }

    // The lock NBT changes on every locked shot; without this, vanilla treats each change as a new item
    // and replays the re-equip "dip" in hand (same fix as IfritFlamethrowerItem).
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.yqils_arknights.typhon_greatbow.description"));
        super.appendHoverText(stack, level, components, flag);
    }

    // Shift + right click = lock on (or cancel an active lock); plain right click = draw the bow like vanilla.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> result;
        boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        if (hasInfinity && player.getProjectile(stack).isEmpty()) {
            // Vanilla bows refuse to draw with zero arrows even with Infinity. This bow allows it (normal and skill
            // shots both fire a plain arrow then; releaseUsing already handles the no-ammo + Infinity case).
            InteractionResultHolder<ItemStack> nock = ForgeEventFactory.onArrowNock(stack, level, player, hand, true);
            if (nock != null) {
                return nock;
            }
            player.startUsingItem(hand);
            result = InteractionResultHolder.consume(stack);
        } else {
            result = super.use(level, player, hand);
        }
        // Skill draw sound: only while a lock is active, once per draw (when the pull actually starts).
        if (!level.isClientSide && hasLock(stack) && player.isUsingItem()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSound.TYPHON_GREATBOW_POWER_DRAW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return result;
    }

    // Shift + left click (sent from the client via TyphonSkillPacket): cancels an active lock, otherwise tries to
    // start the skill. Only this manual toggle cancels -- PRTS goes through activateSkill, which refuses while locked.
    public void toggleSkill(ItemStack stack, ServerLevel level, Player player) {
        if (hasLock(stack)) {
            cancelSkill(stack, level);
        } else {
            activateSkill(stack, level, player);
        }
    }

    // Overrides the shared self-buff default (same idea as TexasBladeItem), so PRTS triggers the lock-on.
    @Override
    public boolean activateSkill(ItemStack stack, Level level, Player player) {
        // Can't re-lock while shots are still loaded (this also blocks PRTS mid-skill).
        if (level.isClientSide || hasLock(stack)
                || !TimedActivation.isReady(stack, level, SKILL_LAST_USED_KEY, SKILL_DURATION_TICKS, SKILL_COOLDOWN_TICKS)) {
            return false;
        }

        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(SCAN_RADIUS),
                        e -> e.isAlive() && isLockable(e))
                .stream()
                .max(Comparator.comparingDouble(LivingEntity::getHealth))
                .orElse(null);

        if (target == null) {
            return false; // nothing to lock -> don't burn the cooldown
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(LOCK_TARGET_KEY, target.getUUID());
        tag.putInt(LOCK_SHOTS_KEY, LOCKED_SHOTS);
        saveLockPos(tag, target.position());
        // No markActivated here -- the cooldown only starts once all locked shots are used.

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, LOCK_GLOW_TICKS, 0));

        // Ground marker that follows the target (removed again in clearLock).
        TyphonLockCircleEntity circle = new TyphonLockCircleEntity(level, target, player);
        level.addFreshEntity(circle);
        tag.putUUID(LOCK_CIRCLE_KEY, circle.getUUID());
        return true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int timeLeft) {
        if (!(shooter instanceof Player player)) return;

        // Checked from NBT only, so client and server agree on which path this shot takes.
        if (!hasLock(stack)) {
            super.releaseUsing(stack, level, shooter, timeLeft); // normal vanilla shot
            return;
        }

        // Ammo / Infinity / enchantments follow vanilla BowItem.releaseUsing, so the skill shot behaves like a
        // normal bow shot apart from where the arrows come from.
        boolean creative = player.getAbilities().instabuild;
        boolean hasInfinity = creative || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        ItemStack ammo = player.getProjectile(stack);
        int drawTicks = ForgeEventFactory.onArrowLoose(stack, level, player, this.getUseDuration(stack) - timeLeft, !ammo.isEmpty() || hasInfinity);
        if (drawTicks < 0) return;
        if (ammo.isEmpty() && !hasInfinity) return;
        if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);

        float power = drawPower(drawTicks, LOCKED_DRAW_TICKS);
        if (power < 0.1F) return; // same "barely drawn" cutoff as vanilla

        // Infinity only covers plain arrows (ArrowItem.isInfinite), exactly like vanilla.
        boolean infiniteAmmo = creative || (ammo.getItem() instanceof ArrowItem ai && ai.isInfinite(ammo, stack, player));
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        boolean flame = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0;

        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = resolveLockCenter(stack, serverLevel);

            // Every living thing inside the circle can be picked (not just hostiles), except the shooter,
            // spectators and armor stands. Other players only when they could be hurt: ServerPlayer.canHarmPlayer
            // = server PvP enabled AND team friendly-fire allows it (same rule vanilla arrows use). Horizontal distance check so the area is the circle, not a box.
            List<LivingEntity> area = serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(SPLASH_RADIUS),
                    e -> e != player && e.isAlive() && !e.isSpectator() && !(e instanceof ArmorStand)
                            && (!(e instanceof Player other) || player.canHarmPlayer(other))
                            && horizontalDistanceSqr(e.position(), center) <= SPLASH_RADIUS * SPLASH_RADIUS);
            ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
            // Snapshot at fire time, same as the arrow's own damage being fixed when it's shot.
            float bonusDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * SKILL_ARROW_ATTACK_RATIO;

            for (int n = 0; n < ARROWS_PER_SHOT; n++) {
                // Each arrow picks its own random target in the area; if none are left, drop on the spot itself.
                LivingEntity victim = area.isEmpty() ? null : area.get(serverLevel.random.nextInt(area.size()));
                Vec3 dropAt = victim == null ? center : victim.position();
                double jx = (serverLevel.random.nextDouble() * 2 - 1) * DROP_JITTER;
                double jz = (serverLevel.random.nextDouble() * 2 - 1) * DROP_JITTER;
                // Start from the top of the victim's head (or just above the circle) and go up HEAD_CLEARANCE
                // (or DROP_HEIGHT), stopping below any ceiling so the arrow never spawns inside a block.
                double baseY = victim == null ? dropAt.y + 0.1D : victim.getY() + victim.getBbHeight();
                double rise = victim == null ? DROP_HEIGHT : HEAD_CLEARANCE;
                double spawnY = clearSpawnY(serverLevel, dropAt.x + jx, baseY, dropAt.z + jz, rise);

                AbstractArrow arrow = arrowItem.createArrow(serverLevel, ammo, player);
                arrow.setPos(dropAt.x + jx, spawnY, dropAt.z + jz);
                // Same speed vanilla uses (power * 3), so damage matches a normal shot (damage = speed * baseDamage).
                arrow.shoot(0.0D, -1.0D, 0.0D, power * 3.0F, 0.0F);
                if (power == 1.0F) arrow.setCritArrow(true);
                if (powerLevel > 0) arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5D + 0.5D);
                if (punchLevel > 0) arrow.setKnockback(punchLevel);
                if (flame) arrow.setSecondsOnFire(100);
                // Skill arrows vanish when they hit a block (ModEventHandlers.onProjectileImpact), so none can
                // actually be picked up; this only matters if that rule is ever removed.
                if (infiniteAmmo || n > 0) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                arrow.getPersistentData().putBoolean(SKILL_ARROW_TAG, true);
                arrow.getPersistentData().putFloat(SKILL_ARROW_BONUS_KEY, bonusDamage);
                serverLevel.addFreshEntity(arrow);
                // Purple burst where each skill arrow appears.
                serverLevel.sendParticles(SPAWN_BURST, arrow.getX(), arrow.getY(), arrow.getZ(),
                        8, 0.2D, 0.15D, 0.2D, 0.0D);
            }

            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            consumeLockedShot(stack, serverLevel);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!infiniteAmmo) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.getInventory().removeItem(ammo);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    // Same curve as vanilla BowItem.getPowerForTime, just stretched to drawTicksForFull ticks.
    private static float drawPower(float ticksDrawn, float drawTicksForFull) {
        float f = ticksDrawn / drawTicksForFull;
        return Math.min((f * f + f * 2.0F) / 3.0F, 1.0F);
    }

    private static float fullDrawTicks(ItemStack stack) {
        return hasLock(stack) ? LOCKED_DRAW_TICKS : NORMAL_DRAW_TICKS;
    }

    // Highest Y up to baseY + rise with a clear line from baseY. If a ceiling is in the way, spawn half a block
    // under it (an arrow's hitbox is 0.5); never lower than just above baseY.
    private static double clearSpawnY(ServerLevel level, double x, double baseY, double z, double rise) {
        Vec3 from = new Vec3(x, baseY, z);
        Vec3 to = new Vec3(x, baseY + rise, z);
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() == HitResult.Type.MISS) {
            return to.y;
        }
        return Math.max(baseY + 0.1D, hit.getLocation().y - 0.5D);
    }

    // While a lock is active, make sure its circle exists (it can be missing after the bow was dropped and picked
    // back up, or for a lock started before circles were saved). Only respawns where the chunk is loaded, so a
    // circle that is merely in an unloaded chunk isn't duplicated.
    private static void ensureLockCircle(ItemStack stack, ServerLevel level, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID(LOCK_CIRCLE_KEY) && level.getEntity(tag.getUUID(LOCK_CIRCLE_KEY)) instanceof TyphonLockCircleEntity) {
            return;
        }
        UUID targetId = tag.hasUUID(LOCK_TARGET_KEY) ? tag.getUUID(LOCK_TARGET_KEY) : null;
        Vec3 pos = targetId != null && level.getEntity(targetId) instanceof LivingEntity target && target.isAlive()
                ? target.position()
                : new Vec3(tag.getDouble(LOCK_X_KEY), tag.getDouble(LOCK_Y_KEY), tag.getDouble(LOCK_Z_KEY));
        if (!level.isLoaded(BlockPos.containing(pos))) {
            return;
        }
        TyphonLockCircleEntity circle = new TyphonLockCircleEntity(level, targetId, pos, player);
        level.addFreshEntity(circle);
        tag.putUUID(LOCK_CIRCLE_KEY, circle.getUUID());
    }

    // Lock-on candidates: vanilla hostiles (Enemy) plus every creature from Torches Become Sunlight, except anything
    // with an owner (that mod's partners and any tamed pet), so the skill never locks onto an ally.
    private static boolean isLockable(LivingEntity e) {
        if (e instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return false;
        }
        if (e instanceof Enemy) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        return id != null && TORCHES_BECOME_SUNLIGHT_MODID.equals(id.getNamespace());
    }

    private static double horizontalDistanceSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x, dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    private static boolean hasLock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(LOCK_SHOTS_KEY) > 0;
    }

    // Follows the locked mob while it's alive; once it's dead/unloaded, keeps using its last seen spot.
    private static Vec3 resolveLockCenter(ItemStack stack, ServerLevel level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID(LOCK_TARGET_KEY)) {
            Entity entity = level.getEntity(tag.getUUID(LOCK_TARGET_KEY));
            if (entity instanceof LivingEntity living && living.isAlive()) {
                saveLockPos(tag, living.position());
                return living.position();
            }
        }
        // Target gone: the lock circle drops to the ground, so rain on wherever it is now.
        if (tag.hasUUID(LOCK_CIRCLE_KEY) && level.getEntity(tag.getUUID(LOCK_CIRCLE_KEY)) instanceof TyphonLockCircleEntity circle) {
            saveLockPos(tag, circle.position());
            return circle.position();
        }
        return new Vec3(tag.getDouble(LOCK_X_KEY), tag.getDouble(LOCK_Y_KEY), tag.getDouble(LOCK_Z_KEY));
    }

    // Ends the skill early: drops all remaining locked shots, removes the lock-on glow, and starts the cooldown now.
    private static void cancelSkill(ItemStack stack, ServerLevel level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID(LOCK_TARGET_KEY)
                && level.getEntity(tag.getUUID(LOCK_TARGET_KEY)) instanceof LivingEntity target) {
            target.removeEffect(MobEffects.GLOWING);
        }
        clearLock(tag, level);
        TimedActivation.markActivated(stack, level, SKILL_LAST_USED_KEY);
    }

    private static void saveLockPos(CompoundTag tag, Vec3 pos) {
        tag.putDouble(LOCK_X_KEY, pos.x);
        tag.putDouble(LOCK_Y_KEY, pos.y);
        tag.putDouble(LOCK_Z_KEY, pos.z);
    }

    private static void consumeLockedShot(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        int left = tag.getInt(LOCK_SHOTS_KEY) - 1;
        if (left <= 0) {
            clearLock(tag, level);
            // Last locked shot fired -> recovery starts now.
            TimedActivation.markActivated(stack, level, SKILL_LAST_USED_KEY);
        } else {
            tag.putInt(LOCK_SHOTS_KEY, left);
        }
    }

    // Used by TyphonLockCircleEntity to check it still belongs to an active lock.
    public static boolean isLockCircle(ItemStack stack, UUID circleId) {
        CompoundTag tag = stack.getTag();
        return stack.getItem() instanceof TyphonGreatbowItem && tag != null
                && tag.hasUUID(LOCK_CIRCLE_KEY) && tag.getUUID(LOCK_CIRCLE_KEY).equals(circleId);
    }

    private static void clearLock(CompoundTag tag, Level level) {
        if (level instanceof ServerLevel serverLevel && tag.hasUUID(LOCK_CIRCLE_KEY)
                && serverLevel.getEntity(tag.getUUID(LOCK_CIRCLE_KEY)) instanceof TyphonLockCircleEntity circle) {
            circle.discard();
        }
        tag.remove(LOCK_CIRCLE_KEY);
        tag.remove(LOCK_TARGET_KEY);
        tag.remove(LOCK_SHOTS_KEY);
        tag.remove(LOCK_X_KEY);
        tag.remove(LOCK_Y_KEY);
        tag.remove(LOCK_Z_KEY);
    }

    // --- HUD: cooldown as usual, locked shots left as the yellow line above it ---
    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        if (hasLock(stack)) {
            return "0/" + SKILL_COOLDOWN_SECONDS; // paused until the locked shots run out
        }
        return TimedActivation.getElapsedSeconds(stack, level, SKILL_LAST_USED_KEY, SKILL_DURATION_TICKS, SKILL_COOLDOWN_SECONDS)
                + "/" + SKILL_COOLDOWN_SECONDS;
    }

    @Override
    public String getCrosshairSecondaryText(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        int left = tag != null ? tag.getInt(LOCK_SHOTS_KEY) : 0;
        return left > 0 ? String.valueOf(left) : null;
    }

    // --- ActivatableSkillItem getters (so PRTS can trigger it) ---
    @Override
    public RegistryObject<MobEffect> getSkillEffect() {
        return null; // unused -- activateSkill is overridden and never applies a self-effect
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
}
