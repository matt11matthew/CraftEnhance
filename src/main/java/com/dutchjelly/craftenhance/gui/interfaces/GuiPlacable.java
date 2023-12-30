package com.dutchjelly.craftenhance.gui.interfaces;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public abstract class GuiPlacable implements ConfigurationSerializable {
   private int page = -1;
   private int slot = -1;
   private int resultSlot;
   private String recipeCategory;

   public GuiPlacable() {
   }

   public GuiPlacable(Map<String, Object> args) {
      this.page = (Integer)args.getOrDefault("page", -1);
      this.slot = (Integer)args.getOrDefault("slot", -1);
      this.resultSlot = (Integer)args.getOrDefault("result_slot", -1);
      this.recipeCategory = (String)args.getOrDefault("category", (Object)null);
   }

   public Map<String, Object> serialize() {
      return new HashMap<String, Object>() {
         {
            this.put("page", GuiPlacable.this.page);
            this.put("slot", GuiPlacable.this.slot);
            this.put("result_slot", GuiPlacable.this.resultSlot);
            this.put("category", GuiPlacable.this.recipeCategory);
         }
      };
   }

   public abstract ItemStack getDisplayItem();

   public int getPage() {
      return this.page;
   }

   public void setPage(int page) {
      this.page = page;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public int getResultSlot() {
      return this.resultSlot;
   }

   public void setResultSlot(int resultSlot) {
      this.resultSlot = resultSlot;
   }

   public String getRecipeCategory() {
      return this.recipeCategory;
   }

   public void setRecipeCategory(String recipeCategory) {
      this.recipeCategory = recipeCategory;
   }
}
