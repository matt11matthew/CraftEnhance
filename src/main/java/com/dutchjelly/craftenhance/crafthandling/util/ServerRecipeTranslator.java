package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class ServerRecipeTranslator {
   public static final String KeyPrefix = "cehrecipe";
   private static final List<String> UsedKeys = new ArrayList();

   public static String GetFreeKey(String seed) {
      Random r = new Random();
      String recipeKey = seed.toLowerCase().replaceAll("[^a-z0-9 ]", "");

      for(recipeKey = recipeKey.trim(); UsedKeys.contains(recipeKey); recipeKey = recipeKey + String.valueOf(r.nextInt(10))) {
      }

      if (!UsedKeys.contains(recipeKey)) {
         UsedKeys.add(recipeKey);
      }

      return recipeKey;
   }

   public static ShapedRecipe translateShapedEnhancedRecipe(ItemStack[] content, ItemStack result, String key) {
      if (!Arrays.asList(content).stream().anyMatch((x) -> {
         return x != null;
      })) {
         return null;
      } else {
         String recipeKey = GetFreeKey(key);

         try {
            ShapedRecipe shaped = Adapter.GetShapedRecipe(CraftEnhance.getPlugin(CraftEnhance.class), "cehrecipe" + recipeKey, result);
            shaped.shape(GetShape(content));
            MapIngredients(shaped, content);
            return shaped;
         } catch (IllegalArgumentException var6) {
            CraftEnhance.self().getLogger().log(Level.WARNING, "Recipe: " + recipeKey + " do have AIR or null as result.");
            return null;
         }
      }
   }

   public static ShapedRecipe translateShapedEnhancedRecipe(WBRecipe recipe) {
      return translateShapedEnhancedRecipe(recipe.getContent(), recipe.getResult(), recipe.getKey());
   }

   public static ShapelessRecipe translateShapelessEnhancedRecipe(ItemStack[] content, ItemStack result, String key) {
      List<ItemStack> ingredients = (List)Arrays.stream(content).filter((x) -> {
         return x != null;
      }).collect(Collectors.toList());
      if (ingredients.size() == 0) {
         return null;
      } else {
         String recipeKey = GetFreeKey(key);
         ShapelessRecipe shapeless = Adapter.GetShapelessRecipe(CraftEnhance.getPlugin(CraftEnhance.class), "cehrecipe" + recipeKey, result);
         ingredients.forEach((x) -> {
            Adapter.AddIngredient(shapeless, x);
         });
         return shapeless;
      }
   }

   public static ShapelessRecipe translateShapelessEnhancedRecipe(WBRecipe recipe) {
      return translateShapelessEnhancedRecipe(recipe.getContent(), recipe.getResult(), recipe.getKey());
   }

   public static ItemStack[] translateShapedRecipe(ShapedRecipe recipe) {
      ItemStack[] content = new ItemStack[9];
      String[] shape = recipe.getShape();

      for(int i = 0; i < shape.length; ++i) {
         int columnIndex = 0;
         char[] var5 = shape[i].toCharArray();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            char c = var5[var7];
            content[i * 3 + columnIndex] = (ItemStack)recipe.getIngredientMap().get(c);
            ++columnIndex;
         }
      }

      return content;
   }

   public static ItemStack[] translateShapelessRecipe(ShapelessRecipe recipe) {
      return recipe != null && recipe.getIngredientList() != null ? (ItemStack[])recipe.getIngredientList().stream().toArray((x$0) -> {
         return new ItemStack[x$0];
      }) : null;
   }

   private static String[] GetShape(ItemStack[] content) {
      String[] recipeShape = new String[]{"", "", ""};

      for(int i = 0; i < 9; ++i) {
         if (content[i] != null) {
            recipeShape[i / 3] = recipeShape[i / 3] + (char)(65 + i);
         } else {
            recipeShape[i / 3] = recipeShape[i / 3] + ' ';
         }
      }

      return TrimShape(recipeShape);
   }

   private static String[] TrimShape(String[] shape) {
      if (shape.length == 0) {
         return shape;
      } else {
         List<String> trimmed = Arrays.asList(shape);

         while(!trimmed.isEmpty() && (((String)trimmed.get(0)).trim().equals("") || ((String)trimmed.get(trimmed.size() - 1)).trim().equals(""))) {
            if (((String)trimmed.get(0)).trim().equals("")) {
               trimmed = trimmed.subList(1, trimmed.size());
            } else {
               trimmed = trimmed.subList(0, trimmed.size() - 1);
            }
         }

         if (trimmed.isEmpty()) {
            throw new IllegalStateException("empty shape is not allowed");
         } else {
            int firstIndex = ((String)trimmed.get(0)).length();
            int lastIndex = 0;

            String line;
            for(Iterator var4 = trimmed.iterator(); var4.hasNext(); lastIndex = Math.max(lastIndex, StringUtils.stripEnd(line, " ").length())) {
               line = (String)var4.next();

               int firstChar;
               for(firstChar = 0; firstChar < line.length() && line.charAt(firstChar) == ' '; ++firstChar) {
               }

               firstIndex = Math.min(firstChar, firstIndex);
            }

//            return trimmed.stream()
//                    .map(x -> x.substring(firstIndex, lastIndex))
//                    .toArray(String[]::new);
            int finalFirstIndex = firstIndex;
            int finalLastIndex = lastIndex;
            return trimmed.stream().map((x) -> {
               return x.substring(finalFirstIndex, finalLastIndex);
            }).toArray((x$0) -> {
               return new String[x$0];
            });
         }
      }
   }

   private static void MapIngredients(ShapedRecipe recipe, ItemStack[] content) {
      for(int i = 0; i < 9; ++i) {
         if (content[i] != null) {
            Adapter.SetIngredient(recipe, (char)(65 + i), content[i]);
         }
      }

   }
}
