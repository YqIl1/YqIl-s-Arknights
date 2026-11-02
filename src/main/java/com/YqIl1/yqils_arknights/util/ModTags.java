package com.YqIl1.yqils_arknights.util;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

public class ModTags {
    public static class Blocks {


        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(Yqils_arknights.MODID, name));
        };
    }
    public static class Items {
        public static final TagKey<Item> WEAPONS = tag("weapons");
        public static final TagKey<Item> SUPPORTS = tag("supports");
        public static final TagKey<Item> NO_MELEE = tag("no_melee");
        public static final TagKey<Item> RANGED = tag("ranged");
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(Yqils_arknights.MODID, name));
        }
    }

    public static class EntityTypes {
        // Extra creatures that count as enemies for auto-aim / enemy checks (see util/EnemyTargeting), e.g.
        // modded hostiles that don't implement Enemy. Entries for mods that aren't installed: {"id": ..., "required": false}.
        public static final TagKey<EntityType<?>> SKILL_TARGETS = tag("skill_targets");
        // Never count as enemies, even if they implement Enemy (e.g. neutral mobs you don't want auto-aimed).
        public static final TagKey<EntityType<?>> SKILL_IGNORED = tag("skill_ignored");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Yqils_arknights.MODID, name));
        }
    }
}
