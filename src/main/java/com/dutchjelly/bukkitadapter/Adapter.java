package com.dutchjelly.bukkitadapter;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.gui.util.SkullCreator;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Adapter {
   public static final String GUI_SKULL_MATERIAL_NAME = "GUI_SKULL_ITEM";
   private static Optional<Boolean> canUseModeldata = Optional.empty();

   public static List<String> CompatibleVersions() {
      return Arrays.asList("1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20");
   }

   public static ItemStack getItemStack(String material, String displayName, List<String> lore, String color, boolean glow) {
      ItemStack item;
      if (color == null) {
         Material mat = getMaterial(material);
         if (mat == null) {
            Messenger.Error("Could not find " + material);
            return null;
         }

         item = new ItemStack(mat);
      } else if (material.equalsIgnoreCase("GUI_SKULL_ITEM")) {
         if (color.startsWith("uuid")) {
            item = SkullCreator.itemFromUuid(UUID.fromString(color.replaceFirst("uuid", "")));
         } else if (color.startsWith("base64")) {
            item = SkullCreator.itemFromBase64(color.replaceFirst("base64", ""));
         } else {
            if (!color.startsWith("url")) {
               throw new ConfigError("specified skull meta is invalid");
            }

            item = SkullCreator.itemFromUrl(color.replaceFirst("url", ""));
         }
      } else {
         DyeColor dColor = dyeColor(color);
         if (dColor == null) {
            throw new ConfigError("color " + color + " not found");
         }

         item = getColoredItem(material, dColor);
      }

      if (item == null) {
         return null;
      } else {
         if (lore != null) {
            lore = (List)lore.stream().map((x) -> {
               return ChatColor.translateAlternateColorCodes('&', x);
            }).collect(Collectors.toList());
         }

         if (displayName != null) {
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
         }

         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            meta.setLore((List)(lore == null ? new ArrayList() : lore));
            meta.setDisplayName(displayName);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
            if (glow) {
               meta.addEnchant(Enchantment.DURABILITY, 10, true);
            }

            item.setItemMeta(meta);
         }

         return item;
      }
   }

   public static DyeColor dyeColor(String dyeColor) {
      DyeColor[] dyeColors = DyeColor.values();
      DyeColor[] var2 = dyeColors;
      int var3 = dyeColors.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         DyeColor color = var2[var4];
         if (color.name().equalsIgnoreCase(dyeColor)) {
            return color;
         }
      }

      return null;
   }

   public static Material getMaterial(String name) {
      if (name == null) {
         return null;
      } else {
         name = name.toUpperCase();
         Material material = Material.getMaterial(name);
         if (material != null) {
            return CraftEnhance.self().getVersionChecker().olderThan(VersionChecker.ServerVersion.v1_13) && name.equals("WRITTEN_BOOK") ? Material.valueOf("PAPER") : material;
         } else {
            byte var3;
            if (CraftEnhance.self().getVersionChecker().newerThan(VersionChecker.ServerVersion.v1_12)) {
               var3 = -1;
               switch(name.hashCode()) {
               case -415523425:
                  if (name.equals("WORKBENCH")) {
                     var3 = 0;
                  }
                  break;
               case 85812:
                  if (name.equals("WEB")) {
                     var3 = 1;
                  }
                  break;
               case 1547712072:
                  if (name.equals("EXP_BOTTLE")) {
                     var3 = 2;
                  }
                  break;
               case 2072873095:
                  if (name.equals("BOOK_AND_QUILL")) {
                     var3 = 3;
                  }
               }

               switch(var3) {
               case 0:
                  return Material.valueOf("CRAFTING_TABLE");
               case 1:
                  return Material.valueOf("COBWEB");
               case 2:
                  return Material.valueOf("EXPERIENCE_BOTTLE");
               case 3:
                  return Material.valueOf("WRITABLE_BOOK");
               default:
                  Messenger.Error("Could not find " + name + " try load legacy suport");
                  return Material.matchMaterial("LEGACY_" + name);
               }
            } else {
               var3 = -1;
               switch(name.hashCode()) {
               case -2140487375:
                  if (name.equals("WRITTEN_BOOK")) {
                     var3 = 5;
                  }
                  break;
               case -1768603919:
                  if (name.equals("CRAFTING_TABLE")) {
                     var3 = 0;
                  }
                  break;
               case -1080169432:
                  if (name.equals("WRITABLE_BOOK")) {
                     var3 = 4;
                  }
                  break;
               case 85812:
                  if (name.equals("WEB")) {
                     var3 = 1;
                  }
                  break;
               case 2044649:
                  if (name.equals("BOOK")) {
                     var3 = 6;
                  }
                  break;
               case 64218094:
                  if (name.equals("CLOCK")) {
                     var3 = 3;
                  }
                  break;
               case 1547712072:
                  if (name.equals("EXP_BOTTLE")) {
                     var3 = 2;
                  }
               }

               switch(var3) {
               case 0:
                  return Material.valueOf("WORKBENCH");
               case 1:
                  return Material.valueOf("COBWEB");
               case 2:
                  return Material.valueOf("EXPERIENCE_BOTTLE");
               case 3:
                  return Material.valueOf("WATCH");
               case 4:
                  return Material.valueOf("BOOK_AND_QUILL");
               case 5:
               case 6:
                  return Material.valueOf("COAL");
               default:
                  return null;
               }
            }
         }
      }
   }

   public static ItemStack getColoredItem(String name, DyeColor color) {
      try {
         return new ItemStack(Material.valueOf(color.name() + "_" + name));
      } catch (Exception var5) {
         try {
            return new ItemStack(Material.valueOf(name), 1, (short)color.getWoolData());
         } catch (Exception var4) {
            return null;
         }
      }
   }

   public static boolean canUseModeldata() {
      if (canUseModeldata.isPresent()) {
         return (Boolean)canUseModeldata.get();
      } else {
         try {
            ItemMeta.class.getMethod("getCustomModelData");
            canUseModeldata = Optional.of(true);
            return true;
         } catch (NoSuchMethodException var1) {
            canUseModeldata = Optional.of(false);
            return false;
         }
      }
   }

   private static Object getNameSpacedKey(JavaPlugin plugin, String key) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      return Class.forName("org.bukkit.NamespacedKey").getConstructor(Plugin.class, String.class).newInstance(plugin, key);
   }

   public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
      try {
         return (ShapedRecipe)ShapedRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException var4) {
         Debug.Send((Object)("Couldn't use namespaced key: " + var4.getMessage() + "\n" + var4.getStackTrace()));
         return new ShapedRecipe(result);
      }
   }

   public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result) {
      try {
         return (ShapelessRecipe)ShapelessRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException var4) {
         Debug.Send((Object)("Couldn't use namespaced key: " + var4.getMessage() + "\n" + var4.getStackTrace()));
         return new ShapelessRecipe(result);
      }
   }

   public static ItemStack SetDurability(ItemStack item, int damage) {
      item.setDurability((short)damage);
      return item;
   }

   public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient) {
      if (CraftEnhance.self().getConfig().getBoolean("learn-recipes")) {
         try {
            recipe.getClass().getMethod("setIngredient", Character.TYPE, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe, key, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient));
         } catch (Exception var4) {
            recipe.setIngredient(key, ingredient.getType());
         }

      } else if (CraftEnhance.self().getVersionChecker().newerThan(VersionChecker.ServerVersion.v1_14)) {
         if (ingredient != null) {
            Material md = ingredient.getType();
            if (md == ingredient.getType() && md != Material.AIR) {
               recipe.setIngredient(key, md);
            } else {
               recipe.setIngredient(key, ingredient.getType());
            }

         }
      } else {
         MaterialData md = ingredient.getData();
         if (md != null && md.getItemType().equals(ingredient.getType()) && !md.getItemType().equals(Material.AIR)) {
            recipe.setIngredient(key, md);
         } else {
            recipe.setIngredient(key, ingredient.getType());
         }

      }
   }

   public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient) {
      if (CraftEnhance.self().getConfig().getBoolean("learn-recipes")) {
         try {
            recipe.getClass().getMethod("addIngredient", Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient));
         } catch (Exception var3) {
            recipe.addIngredient(ingredient.getType());
         }

      } else {
         MaterialData md = ingredient.getData();
         if (md != null && md.getItemType().equals(ingredient.getType()) && !md.getItemType().equals(Material.AIR)) {
            recipe.addIngredient(md);
         } else {
            recipe.addIngredient(ingredient.getType());
         }

      }
   }

   private static <T> boolean callSingleParamMethod(String methodName, T param, Class<T> paramType, Object instance, Class<?> instanceType) {
      try {
         Method m = instanceType.getMethod(methodName, paramType);
         m.invoke(instance, param);
         return true;
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var6) {
         return false;
      }
   }

   public static FurnaceRecipe GetFurnaceRecipe(JavaPlugin plugin, String key, ItemStack result, ItemStack source, int duration, float exp) {
      try {
         return (FurnaceRecipe)FurnaceRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class, Material.class, Float.TYPE, Integer.TYPE).newInstance(getNameSpacedKey(plugin, key), result, source.getType(), exp, duration);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException var8) {
         Debug.Send((Object)("Couldn't use namespaced key: " + var8.getMessage() + "\n" + Arrays.toString(var8.getStackTrace())));
         FurnaceRecipe recipe = new FurnaceRecipe(result, source.getType());
         if (!callSingleParamMethod("setCookingTime", duration, Integer.class, recipe, com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe.class)) {
            Debug.Send((Object)"Custom cooking time is not supported.");
         }

         recipe.setExperience(exp);
         return recipe;
      }
   }

   public static void DiscoverRecipes(Player player, List<Recipe> recipes) {
      try {
         Iterator var2 = recipes.iterator();

         while(var2.hasNext()) {
            Recipe recipe = (Recipe)var2.next();
            if (recipe instanceof ShapedRecipe) {
               ShapedRecipe shaped = (ShapedRecipe)recipe;
               if (shaped.getKey().getNamespace().contains("craftenhance")) {
                  player.discoverRecipe(shaped.getKey());
               }
            } else if (recipe instanceof ShapelessRecipe) {
               ShapelessRecipe shapeless = (ShapelessRecipe)recipe;
               if (shapeless.getKey().getNamespace().contains("craftenhance")) {
                  player.discoverRecipe(shapeless.getKey());
               }
            }
         }
      } catch (Exception var5) {
      }

   }

   public static void SetOwningPlayer(SkullMeta meta, OfflinePlayer player) {
      try {
         meta.setOwningPlayer(player);
      } catch (Exception var3) {
         meta.setOwner(player.getName());
      }

   }

   public static Recipe FilterRecipes(List<Recipe> recipes, String name) {
      Iterator var2 = recipes.iterator();

      Recipe r;
      String id;
      do {
         if (!var2.hasNext()) {
            return (Recipe)recipes.stream().filter((x) -> {
               return x != null;
            }).filter((x) -> {
               return x.getResult().getType().name().equalsIgnoreCase(name);
            }).findFirst().orElse((Recipe) null);
         }

         r = (Recipe)var2.next();
         id = GetRecipeIdentifier(r);
      } while(id == null || !id.equalsIgnoreCase(name));

      return r;
   }

   public static boolean ContainsSubKey(Recipe r, String key) {
      String keyString = GetRecipeIdentifier(r);
      return keyString == null ? key == null : keyString.contains(key);
   }

   public static String GetRecipeIdentifier(Recipe r) {
      try {
         Object obj = r.getClass().getMethod("getKey").invoke(r);
         if (obj != null) {
            return obj.toString();
         }
      } catch (Exception var2) {
      }

      return r.getResult().getType().name();
   }
}
