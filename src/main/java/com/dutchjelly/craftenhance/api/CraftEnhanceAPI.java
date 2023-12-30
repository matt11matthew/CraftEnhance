package com.dutchjelly.craftenhance.api;

import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CraftEnhanceAPI {
   private static final List<CustomCraftListener> customCraftListeners = new ArrayList();

   public static void registerListener(CustomCraftListener listener) {
      customCraftListeners.clear();
      customCraftListeners.add(listener);
   }

   public static boolean fireEvent(EnhancedRecipe recipe, Player p, Inventory craftingInventory, RecipeGroup alternatives) {
      try {
         return customCraftListeners.stream().map((x) -> {
            return x.listener(recipe, p, craftingInventory, alternatives);
         }).anyMatch((x) -> {
            return x;
         });
      } catch (Exception var5) {
         var5.printStackTrace();
         return false;
      }
   }
}
