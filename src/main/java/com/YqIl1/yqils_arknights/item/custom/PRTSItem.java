package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.entity.ai.PrtsOrders;
import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// PRTS: command every loaded mob the player owns (tamed pets, Torches Become Sunlight partners... anything that
// is a Mob + OwnableEntity with this player as owner) in the player's dimension.
// Holding right click opens a radial menu (client/PrtsRadialScreen, opened by client/PrtsClient instead of
// use()); releasing on an entry sends PrtsCommandPacket, which calls issue() here. Orders live on each mob
// (entity/ai/PrtsOrders) and are carried out by PrtsHoldGoal / PrtsAttackTargetGoal, which
// ModEventHandlers#onPrtsGoals adds to every ownable mob.
public class PRTSItem extends Item {
    private static final double MAX_COMMAND_RANGE = 80.0D; // sanity check on the client-picked target/pos
    private static final int TARGET_MARK_TICKS = 60;      // ordered attack target glows for 3 s

    public PRTSItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.yqils_arknights.prts.description"));
        super.appendHoverText(stack, level, components, flag);
    }

    // Right click is intercepted on the client to open the radial menu, so this normally never runs.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    // Server side, from PrtsCommandPacket. targetId = entity id under the crosshair (or -1), pos = block the
    // crosshair hit, already offset to the face that was hit (or null).
    public static void issue(ServerPlayer player, PrtsCommand command, int targetId, @Nullable BlockPos pos) {
        if (!(player.getMainHandItem().getItem() instanceof PRTSItem) && !(player.getOffhandItem().getItem() instanceof PRTSItem)) {
            return;
        }
        ServerLevel level = player.serverLevel();

        if (command == PrtsCommand.SKILLS) {
            // No cooldown of its own: each skill item's own cooldown already limits it.
            activateAllReadySkills(level, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSound.MLYNAR_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        LivingEntity target = null;
        if (command == PrtsCommand.ATTACK) {
            Entity e = level.getEntity(targetId);
            if (!(e instanceof LivingEntity living) || !living.isAlive() || living == player
                    || living.distanceTo(player) > MAX_COMMAND_RANGE || isOwnedBy(living, player)) {
                return;
            }
            target = living;
        }
        Vec3 dest = null;
        if (command == PrtsCommand.MOVE) {
            if (pos == null || !pos.closerToCenterThan(player.position(), MAX_COMMAND_RANGE)) return;
            dest = Vec3.atBottomCenterOf(pos);
        }

        List<Mob> units = ownedMobs(level, player);
        for (Mob mob : units) {
            if (mob == target) continue;
            TamableAnimal tamable = mob instanceof TamableAnimal t ? t : null;
            switch (command) {
                case ATTACK -> {
                    standUp(tamable);
                    PrtsOrders.setAttack(mob, target);
                    mob.setTarget(target);
                }
                case STAY -> {
                    if (tamable != null) {
                        // Tamed animals hold position the vanilla way: sit (no teleporting to the owner either).
                        PrtsOrders.clear(mob);
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                        tamable.setOrderedToSit(true);
                        tamable.setInSittingPose(true);
                    } else {
                        PrtsOrders.setPosOrder(mob, PrtsOrders.STAY, mob.position());
                    }
                }
                case FOLLOW -> {
                    standUp(tamable);
                    PrtsOrders.clear(mob);
                    mob.setTarget(null);
                }
                case MOVE -> {
                    standUp(tamable);
                    PrtsOrders.setPosOrder(mob, PrtsOrders.MOVE, dest);
                }
                default -> {
                }
            }
        }

        if (target != null && !units.isEmpty()) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, TARGET_MARK_TICKS, 0, false, false, false));
        }
        player.displayClientMessage(units.isEmpty()
                ? Component.translatable("message.yqils_arknights.prts.no_units")
                : Component.translatable("message.yqils_arknights.prts." + command.id, units.size()), true);
    }

    private static void standUp(@Nullable TamableAnimal tamable) {
        if (tamable != null) {
            tamable.setOrderedToSit(false);
            tamable.setInSittingPose(false);
        }
    }

    private static boolean isOwnedBy(Entity e, Player player) {
        return e instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID());
    }

    // Every loaded, living Mob in this dimension whose owner is the player.
    private static List<Mob> ownedMobs(ServerLevel level, Player player) {
        List<Mob> result = new ArrayList<>();
        for (Entity e : level.getAllEntities()) {
            if (e instanceof Mob mob && mob.isAlive() && isOwnedBy(mob, player)) {
                result.add(mob);
            }
        }
        return result;
    }

    private static void activateAllReadySkills(Level level, Player player) {
        for (ItemStack stack : player.getInventory().items) {
            tryActivate(stack, level, player);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            tryActivate(stack, level, player);
        }
    }

    private static void tryActivate(ItemStack stack, Level level, Player player) {
        if (stack.getItem() instanceof ActivatableSkillItem skillItem) {
            skillItem.activateSkill(stack, level, player);
        }
    }
}
