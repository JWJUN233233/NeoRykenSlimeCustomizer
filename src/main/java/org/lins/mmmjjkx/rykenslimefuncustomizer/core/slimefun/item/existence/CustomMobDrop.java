package org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.item.existence;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RandomMobDrop;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.item.CustomUnplaceableItem;

public class CustomMobDrop extends CustomUnplaceableItem implements RandomMobDrop {
    private final int chance;

    @Getter
    private final EntityType entityType;

    public CustomMobDrop(ItemGroup itemGroup, SlimefunItemStack item, ItemStack[] recipe, int chance, EntityType type) {
        super(itemGroup, item, RecipeType.MOB_DROP, recipe, null);
        this.chance = chance;
        this.entityType = type;

        register(RykenSlimefunCustomizer.INSTANCE);
    }

    @Override
    public int getMobDropChance() {
        return chance >= 100 ? 100 : Math.max(chance, 1);
    }
}
