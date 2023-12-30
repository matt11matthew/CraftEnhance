package com.dutchjelly.craftenhance.crafthandling.recipes;

import org.bukkit.inventory.Recipe;

public interface ServerLoadable {
   String getKey();

   Recipe getServerRecipe();

   boolean isSimilar(Recipe var1);

   boolean isSimilar(EnhancedRecipe var1);

   boolean isAlwaysSimilar(Recipe var1);
}
