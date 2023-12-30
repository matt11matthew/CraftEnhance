package com.dutchjelly.craftenhance.api.event.crafting;

import com.dutchjelly.craftenhance.api.event.EventUtility;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BeforeCraftOutputEvent extends EventUtility {
   private static final HandlerList handlers = new HandlerList();
   private final EnhancedRecipe eRecipe;
   private final WBRecipe wbRecipe;
   private ItemStack resultItem;
   private boolean cancel;

   public BeforeCraftOutputEvent(EnhancedRecipe eRecipe, WBRecipe wbRecipe, ItemStack resultItem) {
      super(handlers);
      this.eRecipe = eRecipe;
      this.wbRecipe = wbRecipe;
      this.resultItem = resultItem;
      this.registerEvent();
   }

   public EnhancedRecipe geteRecipe() {
      return this.eRecipe;
   }

   public WBRecipe getWbRecipe() {
      return this.wbRecipe;
   }

   public ItemStack getResultItem() {
      return this.resultItem;
   }

   public void setResultItem(ItemStack resultItem) {
      this.resultItem = resultItem;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
