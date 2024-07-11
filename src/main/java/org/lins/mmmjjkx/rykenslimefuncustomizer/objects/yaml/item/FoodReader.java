package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item.CustomFood;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class FoodReader extends YamlReader<CustomFood> {
    public FoodReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @Override
    public CustomFood readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        String id = section.getString("id_alias", s);

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");

        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        SlimefunItemStack sfis = getPreloadItem(id);
        if (sfis == null) return null;

        ItemStack[] itemStacks = CommonUtils.readRecipe(section.getConfigurationSection("recipe"), addon);
        String recipeType = section.getString("recipe_type", "NULL");

        Pair<ExceptionHandler.HandleResult, RecipeType> rt = ExceptionHandler.getRecipeType(
                "在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "错误的配方类型" + recipeType + "!", recipeType);

        if (rt.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        JavaScriptEval eval = null;
        if (section.contains("script")) {
            String script = section.getString("script", "");
            File file = new File(addon.getScriptsFolder(), script + ".js");
            if (!file.exists()) {
                ExceptionHandler.handleWarning(
                        "在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "找不到脚本文件 " + file.getName());
            } else {
                eval = new JavaScriptEval(file, addon);
            }
        }

        if (CommonUtils.versionToCode(Bukkit.getMinecraftVersion()) >= 1205) {
            if (Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
                sfis = nbtApply(id, section, sfis);
            }
        }

        return new CustomFood(group.getSecondValue(), sfis, rt.getSecondValue(), itemStacks, eval);
    }

    private SlimefunItemStack nbtApply(String s, ConfigurationSection section, SlimefunItemStack sfis) {
        NBTItem nbti = new NBTItem(sfis);
        NBTCompound food = nbti.getOrCreateCompound("food");
        int nutrition = section.getInt("nutrition");
        float saturation = section.getInt("saturation");
        boolean alwaysEatable = section.getBoolean("always_eatable", false);
        float eatseconds = section.getInt("eat_seconds", 0);
        if (nutrition < 1) {
            ExceptionHandler.handleError(
                    "在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "饥饿值 " + nutrition + "小于1! 已转为1");
            nutrition = 1;
        }
        if (saturation < 0f) {
            ExceptionHandler.handleError(
                    "在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "饱和度 " + saturation + "小于0! 已转为0");
            saturation = 0f;
        }
        if (eatseconds < 0) {
            ExceptionHandler.handleError(
                    "在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "食用时间 " + eatseconds + "小于0! 已转为1.6");
            eatseconds = 1.6f;
        }
        food.setInteger("nutrition", nutrition);
        food.setFloat("saturation", saturation);
        food.setBoolean("can_always_eat", alwaysEatable);
        food.setFloat("eat_seconds", eatseconds);

        return new SlimefunItemStack(sfis.getItemId(), nbti.getItem());
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);

        if (section == null) return null;

        String id = section.getString(s + ".id_alias", s);

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);
        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载食物" + s + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }

        SlimefunItemStack sfis = new SlimefunItemStack(id, stack);

        return List.of(sfis);
    }
}
