package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class WBRecipe extends EnhancedRecipe {
   private boolean shapeless = false;
   private final RecipeType type;
   private Recipe recipe;

   public WBRecipe(String perm, ItemStack result, ItemStack[] content) {
      super(perm, result, content);
      this.type = RecipeType.WORKBENCH;
   }

   private WBRecipe(Map<String, Object> args) {
      super(args);
      this.type = RecipeType.WORKBENCH;
      if (args.containsKey("shapeless")) {
         this.shapeless = (Boolean)args.get("shapeless");
      }

   }

   public WBRecipe() {
      this.type = RecipeType.WORKBENCH;
   }

   public static WBRecipe deserialize(Map<String, Object> args) {
      WBRecipe recipe = new WBRecipe(args);
      if (args.containsKey("shapeless")) {
         recipe.shapeless = (Boolean)args.get("shapeless");
      }

      return recipe;
   }

   public Map<String, Object> serialize() {
      return new HashMap<String, Object>() {
         {
            this.putAll(WBRecipe.super.serialize());
            this.put("shapeless", WBRecipe.this.shapeless);
         }
      };
   }

   public Recipe getServerRecipe() {
      if (this.recipe == null) {
         System.out.println("getServerRecipe shapeless" + this.shapeless);
         if (this.shapeless) {
            this.recipe = ServerRecipeTranslator.translateShapelessEnhancedRecipe(this);
         } else {
            this.recipe = ServerRecipeTranslator.translateShapedEnhancedRecipe(this);
         }
      }

      return this.recipe;
   }

   public boolean isSimilar(Recipe r) {
      ItemStack[] shapedContent;
      if (r instanceof ShapelessRecipe) {
         shapedContent = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)r);
         boolean result = WBRecipeComparer.ingredientsMatch(this.getContent(), shapedContent, ItemMatchers::matchType);
         return result;
      } else if (r instanceof ShapedRecipe) {
         shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
         return this.shapeless ? WBRecipeComparer.ingredientsMatch(shapedContent, this.getContent(), ItemMatchers::matchType) : WBRecipeComparer.shapeMatches(this.getContent(), shapedContent, ItemMatchers::matchType);
      } else {
         return false;
      }
   }

   public boolean isAlwaysSimilar(Recipe r) {
      if (!ItemMatchers.matchItems(r.getResult(), this.getResult())) {
         return false;
      } else {
         ItemStack[] shapedContent;
         if (r instanceof ShapelessRecipe) {
            shapedContent = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)r);
            return WBRecipeComparer.ingredientsMatch(this.getContent(), shapedContent, ItemMatchers::matchTypeData);
         } else if (r instanceof ShapedRecipe && !this.shapeless) {
            shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            return WBRecipeComparer.shapeMatches(this.getContent(), shapedContent, ItemMatchers::matchTypeData);
         } else {
            return false;
         }
      }
   }

   public boolean isEqual(Recipe r) {
      ItemStack[] shapedContent;
      if (r instanceof ShapelessRecipe) {
         if (!this.shapeless) {
            return false;
         } else {
            shapedContent = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)r);
            boolean result = WBRecipeComparer.ingredientsMatch(this.getContent(), shapedContent, ItemMatchers::matchType);
            return result;
         }
      } else if (r instanceof ShapedRecipe) {
         shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
         return this.shapeless ? WBRecipeComparer.ingredientsMatch(shapedContent, this.getContent(), ItemMatchers::matchType) : WBRecipeComparer.shapeMatches(this.getContent(), shapedContent, ItemMatchers::matchType);
      } else {
         return false;
      }
   }

   public boolean isSimilar(EnhancedRecipe r) {
      if (r == null) {
         return false;
      } else if (!(r instanceof WBRecipe)) {
         return false;
      } else {
         WBRecipe wbr = (WBRecipe)r;
         return !wbr.isShapeless() && !this.shapeless ? WBRecipeComparer.shapeMatches(this.getContent(), wbr.getContent(), ItemMatchers::matchType) : WBRecipeComparer.ingredientsMatch(this.getContent(), wbr.getContent(), ItemMatchers::matchType);
      }
   }

   public boolean matches(ItemStack[] content) {
      if (this.isShapeless() && WBRecipeComparer.ingredientsMatch(content, this.getContent(), this.getMatchType().getMatcher())) {
         return true;
      } else {
         return !this.isShapeless() && WBRecipeComparer.shapeMatches(content, this.getContent(), this.getMatchType().getMatcher());
      }
   }

   public boolean isShapeless() {
      return this.shapeless;
   }

   public void setShapeless(boolean shapeless) {
      this.shapeless = shapeless;
   }

   public RecipeType getType() {
      return this.type;
   }
}
