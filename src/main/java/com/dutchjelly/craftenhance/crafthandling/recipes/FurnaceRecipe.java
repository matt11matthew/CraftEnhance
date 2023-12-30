package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class FurnaceRecipe extends EnhancedRecipe {
   private int duration = 160;
   private float exp = 0.0F;
   private final RecipeType type;

   private FurnaceRecipe(Map<String, Object> args) {
      super(args);
      this.type = RecipeType.FURNACE;
   }

   public FurnaceRecipe(String perm, ItemStack result, ItemStack[] content) {
      super(perm, result, content);
      this.type = RecipeType.FURNACE;
   }

   public static FurnaceRecipe deserialize(Map<String, Object> args) {
      FurnaceRecipe recipe = new FurnaceRecipe(args);
      recipe.duration = (Integer)args.get("duration");
      recipe.exp = (float)args.get("exp");
      return recipe;
   }

   public Map<String, Object> serialize() {
      return new HashMap<String, Object>() {
         {
            this.putAll(FurnaceRecipe.super.serialize());
            this.put("exp", FurnaceRecipe.this.exp);
            this.put("duration", FurnaceRecipe.this.duration);
         }
      };
   }

   public boolean matches(ItemStack[] content) {
      return content.length == 1 && this.getMatchType().getMatcher().match(content[0], this.getContent()[0]);
   }

   public boolean matcheType(ItemStack[] content) {
      return content.length == 1 && ItemMatchers.matchType(content[0], this.getContent()[0]);
   }

   public Recipe getServerRecipe() {
      return Adapter.GetFurnaceRecipe(CraftEnhance.self(), ServerRecipeTranslator.GetFreeKey(this.getKey()), this.getResult(), this.getContent()[0], this.getDuration(), this.getExp());
   }

   public boolean isSimilar(Recipe r) {
      if (!(r instanceof org.bukkit.inventory.FurnaceRecipe)) {
         return false;
      } else {
         org.bukkit.inventory.FurnaceRecipe serverRecipe = (org.bukkit.inventory.FurnaceRecipe)r;
         return ItemMatchers.matchType(serverRecipe.getInput(), this.getContent()[0]) && ItemMatchers.matchType(serverRecipe.getResult(), this.getResult());
      }
   }

   public boolean isSimilar(EnhancedRecipe r) {
      return r instanceof FurnaceRecipe && ItemMatchers.matchTypeData(r.getContent()[0], this.getContent()[0]);
   }

   public boolean isAlwaysSimilar(Recipe r) {
      if (!ItemMatchers.matchItems(r.getResult(), this.getResult())) {
         return false;
      } else {
         return !(r instanceof org.bukkit.inventory.FurnaceRecipe) ? false : this.isSimilar(r);
      }
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public float getExp() {
      return this.exp;
   }

   public void setExp(float exp) {
      this.exp = exp;
   }

   public RecipeType getType() {
      return this.type;
   }
}
