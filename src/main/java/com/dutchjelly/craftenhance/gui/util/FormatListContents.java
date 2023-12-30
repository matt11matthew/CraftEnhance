package com.dutchjelly.craftenhance.gui.util;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.util.PermissionTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

public class FormatListContents {
   public static <RecipeT extends EnhancedRecipe> List<?> formatRecipes(RecipeT recipe) {
      if (recipe == null) {
         return new ArrayList();
      } else {
         List<Object> list = new ArrayList(Arrays.asList(recipe.getContent()));
         byte index;
         if (list.size() < 6) {
            index = 1;
         } else {
            index = 6;
         }

         list.add(index, recipe.getResult());
         return list;
      }
   }

   public static List<EnhancedRecipe> canSeeRecipes(List<EnhancedRecipe> enhancedRecipes, Player p) {
      return (List)enhancedRecipes.stream().filter((x) -> {
         return (!CraftEnhance.self().getConfig().getBoolean("only-show-available") || x.getPermissions() == null || Objects.equals(x.getPermissions(), "") || p.hasPermission(x.getPermissions())) && (!x.isHidden() || p.hasPermission(PermissionTypes.Edit.getPerm()) || p.hasPermission(x.getPermissions() + ".hidden"));
      }).collect(Collectors.toList());
   }

   public static List<CategoryData> getCategorys(Collection<CategoryData> categoryData, String grupSeachFor) {
      return (List)(grupSeachFor != null && !grupSeachFor.equals("") ? (List)categoryData.stream().filter((x) -> {
         return x.getRecipeCategory().contains(grupSeachFor);
      }).collect(Collectors.toList()) : new ArrayList(categoryData));
   }

   public static List<String> getCategorys(Set<String> categoryNames, String grupSeachFor) {
      return (List)(grupSeachFor != null && !grupSeachFor.equals("") ? (List)categoryNames.stream().filter((x) -> {
         return x.contains(grupSeachFor);
      }).collect(Collectors.toList()) : new ArrayList(categoryNames));
   }

   public static List<Recipe> getRecipes(List<Recipe> enabledRecipes, List<Recipe> disabledRecipes, boolean enableMode, String grupSeachFor) {
      List<Recipe> recipes = !enableMode ? enabledRecipes : disabledRecipes;
      return grupSeachFor != null && !grupSeachFor.equals("") ? (List)recipes.stream().filter((recipe) -> {
         return recipe.getResult().getType().name().contains(grupSeachFor.toUpperCase());
      }).collect(Collectors.toList()) : recipes;
   }
}
