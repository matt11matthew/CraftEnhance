package com.dutchjelly.craftenhance.api;

import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface CustomCraftListener {
   boolean listener(EnhancedRecipe var1, Player var2, Inventory var3, RecipeGroup var4);
}
