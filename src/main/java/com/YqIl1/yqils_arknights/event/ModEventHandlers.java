package com.YqIl1.yqils_arknights.event;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.effect.custom.MlynarPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.NCDeerFanPowerEffect;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.ModTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraftforge.fml.common.Mod;

// Defaults to the Forge event bus (game events), unlike the mod event bus used for registration/setup
@Mod.EventBusSubscriber(modid = Yqils_arknights.MODID)
public class ModEventHandlers {
    private static final String SILENCED_HIT_TICK_KEY = "yqils_arknights:silenced_hit_tick";
    private static final String TYPHON_HIT_SOUND_TICK_KEY = "yqils_arknights:typhon_hit_sound_tick";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        // Typhon skill arrows: add ATTACK_DAMAGE * 1.75 on top of the arrow's own damage, and stun for 0.4s.
        // Done first in this handler so Fragile and the damage reductions below apply to the full amount.
        var direct = event.getSource().getDirectEntity();
        if (direct != null && direct.getPersistentData().getBoolean(TyphonGreatbowItem.SKILL_ARROW_TAG)) {
            event.setAmount(event.getAmount() + direct.getPersistentData().getFloat(TyphonGreatbowItem.SKILL_ARROW_BONUS_KEY));
            entity.addEffect(new MobEffectInstance(ModEffects.STUN.get(), TyphonGreatbowItem.STUN_TICKS, 0));

            // Skill hit sound -- at most once per target per tick, since 5 arrows often land together.
            var data = entity.getPersistentData();
            long now = entity.level().getGameTime();
            if (data.getLong(TYPHON_HIT_SOUND_TICK_KEY) != now) {
                data.putLong(TYPHON_HIT_SOUND_TICK_KEY, now);
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSound.TYPHON_GREATBOW_POWER_HIT.get(), SoundSource.PLAYERS, 1.0F,
                        0.9F + entity.getRandom().nextFloat() * 0.2F);
            }
        }

        if (entity.hasEffect(ModEffects.FRAGILE.get())) {
            MobEffectInstance instance = entity.getEffect(ModEffects.FRAGILE.get());
            int level = instance.getAmplifier() + 1;
            float multiplier = 1.0F + (0.10F * level);
            event.setAmount(event.getAmount() * multiplier);
        }

        if (entity.hasEffect(ModEffects.MLYNAR_POWER.get())
                && entity.getPersistentData().getBoolean(MlynarPowerEffect.THRESHOLD_MET_KEY)) {
            event.setAmount(event.getAmount() * (1.0F - MlynarPowerEffect.THRESHOLD_DAMAGE_REDUCTION));
        }

        if (entity.hasEffect(ModEffects.NC_DEER_FAN_POWER.get())) {
            event.setAmount(event.getAmount() * (1.0F - NCDeerFanPowerEffect.DAMAGE_REDUCTION));
        }
    }

    // Vanilla hardcodes melee damage to damageSources().playerAttack(...) inside Player.attack(),
    // with no item-level hook to change the damage type -- so while Lappland's Power is active,
    // cancel the original hit and re-apply the same amount through a magic damage source instead.
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof LapplandBladeItem)) {
            return;
        }
        if (!player.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get())) {
            return;
        }
        LivingEntity target = event.getEntity();
        float amount = event.getAmount();
        event.setCanceled(true);
        target.hurt(player.damageSources().indirectMagic(player, player), amount);
        player.getMainHandItem().hurtEnemy(target, player);
    }

    // Items tagged no_melee (wand, flamethrower, greatbow) can't melee at all. AttackEntityEvent fires at the very
    // start of Player.attack, so cancelling it stops the damage AND the knockback, hurt flash, sweep and durability
    // loss. (The old LivingDamageEvent cancel only zeroed the damage; knockback etc. still happened.)
    @SubscribeEvent
    public static void onNoMeleeAttack(AttackEntityEvent event) {
        if (event.getEntity().getMainHandItem().is(ModTags.Items.NO_MELEE)) {
            event.setCanceled(true);
        }
    }

    // Each kill while Unbrilliant Glory is active decays its attack damage boost, capped at 20 stacks
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (!player.hasEffect(ModEffects.MLYNAR_POWER.get())) {
            return;
        }

        var data = player.getPersistentData();
        int kills = data.getInt(MlynarPowerEffect.KILL_COUNT_KEY);
        data.putInt(MlynarPowerEffect.KILL_COUNT_KEY, Math.min(kills + 1, 20));
    }

    // Reset the decay counter whenever Unbrilliant Glory is (re)applied, so re-casting it clears past decay
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == ModEffects.MLYNAR_POWER.get()) {
            event.getEntity().getPersistentData().putInt(MlynarPowerEffect.KILL_COUNT_KEY, 0);
        }
    }

    // Typhon's Greatbow drops 5 arrows per skill shot, often onto the same mob. Vanilla's hurt cooldown
    // (invulnerableTime) would make every arrow after the first fail and bounce off, so clear it right
    // before a skill arrow lands.
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!event.getProjectile().getPersistentData().getBoolean(TyphonGreatbowItem.SKILL_ARROW_TAG)) {
            return;
        }
        if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            target.invulnerableTime = 0;
        } else if (event.getRayTraceResult().getType() == HitResult.Type.BLOCK) {
            // Skill arrows vanish the moment they hit a block instead of sticking in the ground, so the
            // rain never leaves a pile of arrows behind. (Arrows that hit a creature are removed by vanilla.)
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        }
    }

    // ===================== Silence (see SilenceEffect for the full rule list) =====================

    private static boolean isSilenced(@Nullable Entity entity) {
        return entity instanceof LivingEntity living && living.hasEffect(ModEffects.SILENCE.get());
    }

    // Bosses (forge:bosses tag -- Wither, Ender Dragon, and any mod boss that adds itself) keep their ranged
    // attacks while silenced: the projectile / non-melee damage / explosion rules below skip them. Everything
    // else (buff stripping, no new buffs, no teleport...) still applies.
    // Also anything listed in the config's silenceBossLikeEntities.
    private static boolean isBoss(@Nullable Entity entity) {
        return entity != null && (entity.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)
                || com.YqIl1.yqils_arknights.Config.isSilenceBossLike(entity));
    }

    private static boolean isSilencedNonBoss(@Nullable Entity entity) {
        return isSilenced(entity) && !isBoss(entity);
    }

    // Strip beneficial effects every tick, and keep creepers from fusing. Done here (start of
    // LivingEntity.tick, before tickEffects iterates the effect map) instead of in the effect's own tick.
    @SubscribeEvent
    public static void onSilencedTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !entity.hasEffect(ModEffects.SILENCE.get())) {
            return;
        }
        List<MobEffect> beneficial = entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect).filter(MobEffect::isBeneficial).toList();
        beneficial.forEach(entity::removeEffect);
        if (entity instanceof Creeper creeper) {
            creeper.setTarget(null);     // no target -> SwellGoal sets the swell direction back to -1
            creeper.setSwellDir(-1);
        }
    }

    // No new beneficial effects while silenced; and harmful on-hit effects from a silenced attacker's melee
    // (wither skeleton, cave spider, husk...) are blocked for the tick that hit landed.
    @SubscribeEvent
    public static void onSilenceEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffect effect = event.getEffectInstance().getEffect();
        if (effect.isBeneficial() && entity.hasEffect(ModEffects.SILENCE.get())) {
            event.setResult(Event.Result.DENY);
        } else if (effect.getCategory() == MobEffectCategory.HARMFUL && effect != ModEffects.SILENCE.get()
                && entity.getPersistentData().getLong(SILENCED_HIT_TICK_KEY) == entity.level().getGameTime()) {
            event.setResult(Event.Result.DENY);
        }
    }

    // A silenced attacker can only deal its own plain melee damage and normal projectile hits (arrows,
    // fireballs, tridents...). Everything else it causes (guardian laser, warden sonic boom, fangs,
    // explosions, magic...) is cancelled. Harmful on-hit effects from either are still blocked below.
    @SubscribeEvent
    public static void onSilencedAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || !attacker.hasEffect(ModEffects.SILENCE.get())) {
            return;
        }
        if (isBoss(attacker)) {
            return; // bosses keep their ranged / special damage
        }
        boolean plainMelee = source.getDirectEntity() == attacker
                && (source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.PLAYER_ATTACK));
        boolean projectileHit = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
        if (!plainMelee && !projectileHit) {
            event.setCanceled(true);
            return;
        }
        LivingEntity target = event.getEntity();
        target.getPersistentData().putLong(SILENCED_HIT_TICK_KEY, target.level().getGameTime());
    }

    // Silenced creatures can't create evoker fangs, vexes or lingering effect clouds. (Projectiles are
    // allowed, and hit normally -- see onSilencedAttack.)
    @SubscribeEvent
    public static void onSilencedSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity spawned = event.getEntity();

        // Slime / magma cube split. The children are created inside the dying parent's remove(), before the
        // parent leaves the level, so the parent is still findable here: same type, dying, exactly twice the
        // child's size.
        //  - silenced parent -> no split at all
        //  - stunned parent  -> children would copy its NoAi=true (set by StunEffect) and stay frozen forever
        //                       without ever having the effect that clears it, so give them their AI back
        if (spawned instanceof Slime child) {
            List<Slime> parents = event.getLevel().getEntitiesOfClass(Slime.class,
                    child.getBoundingBox().inflate(child.getSize() * 2.0D),
                    parent -> parent != child && parent.getType() == child.getType() && parent.isDeadOrDying()
                            && parent.getSize() / 2 == child.getSize());
            if (parents.stream().anyMatch(parent -> parent.hasEffect(ModEffects.SILENCE.get()))) {
                event.setCanceled(true);
            } else if (parents.stream().anyMatch(parent -> parent.hasEffect(ModEffects.STUN.get()))) {
                child.setNoAi(false);
            }
            return;
        }

        Entity owner = null;
        if (spawned instanceof EvokerFangs fangs) owner = fangs.getOwner();
        else if (spawned instanceof Vex vex) owner = vex.getOwner();
        else if (spawned instanceof AreaEffectCloud cloud) owner = cloud.getOwner();
        if (isSilencedNonBoss(owner)) {
            event.setCanceled(true);
        }
    }

    // Compat: whenever our Silence lands, also apply Caerula Arbor's own silence (caerula_arbor:mute) with the
    // same duration/level, so that mod's creatures and abilities respect it too. Looked up by registry name,
    // so there's no compile dependency and nothing happens when Caerula Arbor isn't installed.
    private static final net.minecraft.resources.ResourceLocation CAERULA_ARBOR_MUTE =
            new net.minecraft.resources.ResourceLocation("caerula_arbor", "mute");

    @SubscribeEvent
    public static void onSilenceAdded(MobEffectEvent.Added event) {
        MobEffectInstance added = event.getEffectInstance();
        LivingEntity entity = event.getEntity();
        if (added.getEffect() != ModEffects.SILENCE.get() || entity.level().isClientSide) {
            return;
        }
        MobEffect mute = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(CAERULA_ARBOR_MUTE);
        if (mute != null) {
            entity.addEffect(new MobEffectInstance(mute, added.getDuration(), added.getAmplifier()), event.getEffectSource());
        }
    }

    // Endermen and shulkers can't teleport while silenced.
    @SubscribeEvent
    public static void onSilencedTeleport(EntityTeleportEvent.EnderEntity event) {
        if (isSilenced(event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    // Silenced creepers can't pick a target, so they can't start fusing.
    @SubscribeEvent
    public static void onSilencedTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Creeper && event.getNewTarget() != null && isSilenced(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Safety net: no explosions caused by a silenced creature.
    @SubscribeEvent
    public static void onSilencedExplosion(ExplosionEvent.Start event) {
        if (isSilencedNonBoss(event.getExplosion().getIndirectSourceEntity()) || isSilencedNonBoss(event.getExplosion().getExploder())) {
            event.setCanceled(true);
        }
    }

    // Mob conversions (villager -> witch, zombie -> drowned, piglin -> zombified piglin, skeleton -> stray,
    // zombie villager cure...) copy the original's NoAi flag via Mob.convertTo. If the original was stunned,
    // that NoAi=true came from StunEffect, so the converted mob would stay frozen forever -- clear it.
    @SubscribeEvent
    public static void onStunnedConversion(LivingConversionEvent.Post event) {
        if (event.getEntity().hasEffect(ModEffects.STUN.get()) && event.getOutcome() instanceof Mob outcome) {
            outcome.setNoAi(false);
        }
    }

    // Ceobe's Spear: auto-throw at full charge and restart the charge (see CeobeSpearItem#tickThrowCharge).
    // Using the event's duration (rather than stop/startUsingItem) resets the hold cleanly on both sides.
    @SubscribeEvent
    public static void onCeobeSpearChargeTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getItem().getItem() instanceof com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem spear
                && event.getEntity() instanceof Player player) {
            event.setDuration(spear.tickThrowCharge(event.getItem(), player, event.getDuration()));
        }
    }

    //Silenced death: block delayed death effects
    private static final double DEATH_EXPLOSION_RADIUS = 2.0D; // explosions centred this close to the corpse

    private static boolean isDyingSilenced(@Nullable Entity entity) {
        return entity instanceof LivingEntity living && living.isDeadOrDying() && isSilencedNonBoss(living);
    }

    // Any dying silenced creature within `radius` of `pos` in this level.
    private static boolean dyingSilencedNear(Level level, Vec3 pos, double radius) {
        AABB box = new AABB(pos, pos).inflate(radius);
        return !level.getEntitiesOfClass(LivingEntity.class, box, ModEventHandlers::isDyingSilenced).isEmpty();
    }

    // Explosions caused by the corpse, or centred right on it (some mods explode with no source).
    @SubscribeEvent
    public static void onSilencedDeathExplosion(ExplosionEvent.Start event) {
        Explosion explosion = event.getExplosion();
        if (isDyingSilenced(explosion.getIndirectSourceEntity()) || isDyingSilenced(explosion.getExploder())
                || dyingSilencedNear(event.getLevel(), explosion.getPosition(), DEATH_EXPLOSION_RADIUS)) {
            event.setCanceled(true);
        }
    }

    // Any damage dealt by the corpse -- including melee-typed damage, which a *living* silenced mob is
    // still allowed to deal (see onSilencedAttack).
    @SubscribeEvent
    public static void onSilencedDeathDamage(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (isDyingSilenced(source.getEntity()) || isDyingSilenced(source.getDirectEntity())) {
            event.setCanceled(true);
        }
    }

    // Block changes the corpse makes through Forge's hooks (placing blocks/fire, mob griefing).
    @SubscribeEvent
    public static void onSilencedDeathPlace(BlockEvent.EntityPlaceEvent event) {
        if (isDyingSilenced(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSilencedDeathGriefing(EntityMobGriefingEvent event) {
        if (isDyingSilenced(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
