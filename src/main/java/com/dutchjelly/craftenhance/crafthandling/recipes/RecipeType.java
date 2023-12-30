package com.dutchjelly.craftenhance.crafthandling.recipes;

import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public enum RecipeType {
   WORKBENCH,
   FURNACE;

   public static RecipeType getType(Recipe r) {
      if (r instanceof ShapedRecipe) {
         return WORKBENCH;
      } else if (r instanceof ShapelessRecipe) {
         return WORKBENCH;
      } else {
         return r instanceof org.bukkit.inventory.FurnaceRecipe ? FURNACE : null;
      }
   }
}
